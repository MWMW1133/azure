package com.azure.service.impl;

import com.azure.model.calendar.PersonalCalendar;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.reminder.Reminder;
import com.azure.repository.PersonalCalendarRepository;
import com.azure.repository.ProjectCalendarRepository;
import com.azure.repository.ReminderRepository;
import com.azure.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 매 30초마다 '도래한 리마인더'를 찾아 notifications 레코드를 생성하고
 * (선택) 웹소켓으로 푸시한다.
 *
 * DB 변경 없이 중복 방지를 위해 70초 TTL의 인메모리 캐시를 사용한다.
 * - 앱 재시작 시 한 번 더 보낼 수 있음(과제/프로젝트 용도로 OK)
 */
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final ReminderRepository reminderRepository;
    private final ProjectCalendarRepository projectCalendarRepository;
    private final PersonalCalendarRepository personalCalendarRepository;
    private final NotificationService notificationService;
    // 리마인더 조회 시점부터 몇 초 이내에 시작하는 일정들을 찾을지
    private static final int WINDOW_SEC = 60;

    // 간단한 중복 방지: reminderId -> lastSentEpochMillis
    private final Map<Long, Long> sentMap = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 30_000)    // 30초마다
    @Transactional(readOnly = true)
    public void tick() {
        long now = System.currentTimeMillis();
        // 메모리 캐시 청소(70초 지난 키 제거)
        sentMap.entrySet().removeIf(e -> now - e.getValue() > 70_000);

        // 1) 프로젝트 일정 리마인더
        List<Reminder> pr = reminderRepository.findDueProjectReminders(WINDOW_SEC);
        for (Reminder r : pr) {
            if (alreadySent(r.getId())) continue;
            ProjectCalendar ev = projectCalendarRepository.findById(r.getProjectEvent().getId()).orElse(null);
            if (ev == null) continue;

            String when = ev.getStartAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
            String payload = String.format("[프로젝트 일정] '%s'가 %d분 후(%s)에 시작합니다.",
                    nullToDash(ev.getTitle()), r.getMinutesBefore(), when);

            notificationService.notifyUser(r.getUser().getId(), "PROJECT_EVENT_REMINDER", payload);
            markSent(r.getId());
        }

        // 2) 개인 일정 리마인더
        List<Reminder> prsn = reminderRepository.findDuePersonalReminders(WINDOW_SEC);
        for (Reminder r : prsn) {
            if (alreadySent(r.getId())) continue;
            PersonalCalendar ev = personalCalendarRepository.findById(r.getPersonalEvent().getId()).orElse(null);
            if (ev == null) continue;

            String when = ev.getStartAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
            String payload = String.format("[개인 일정] '%s'가 %d분 후(%s)에 시작합니다.",
                    nullToDash(ev.getTitle()), r.getMinutesBefore(), when);

            notificationService.notifyUser(r.getUser().getId(), "PERSONAL_EVENT_REMINDER", payload);
            markSent(r.getId());
        }
    }

    private boolean alreadySent(Long reminderId) {
        Long t = sentMap.get(reminderId);
        return t != null && System.currentTimeMillis() - t < 70_000;
        // 70초 이내에 한 번 보냈으면 중복 방지
    }
    private void markSent(Long reminderId) { sentMap.put(reminderId, System.currentTimeMillis()); }
    private static String nullToDash(String s) { return (s == null || s.isBlank()) ? "-" : s; }
}
