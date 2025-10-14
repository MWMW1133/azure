package com.azure.service.impl;

import com.azure.model.calendar.EventAttendee;
import com.azure.model.calendar.PersonalCalendar;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.reminder.Reminder;
import com.azure.repository.EventAttendeeRepository;
import com.azure.repository.PersonalCalendarRepository;
import com.azure.repository.ProjectCalendarRepository;
import com.azure.repository.ReminderRepository;
import com.azure.service.CalendarService;
import com.azure.service.exception.BadRequestException;
import com.azure.service.exception.NotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.azure.model.user.User;

@Service
@Transactional
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final ProjectCalendarRepository projectCalendarRepository;
    private final PersonalCalendarRepository personalCalendarRepository;
    private final ReminderRepository reminderRepository;
    private final EventAttendeeRepository eventAttendeeRepository;
    @PersistenceContext
    private EntityManager em;
    /* ========= 유틸 ========= */

    /** 시작/종료 시간 검증 (둘 다 필수, end > start) */
    private void validateTimeRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new BadRequestException("startAt과 endAt은 필수입니다.");
        }
        if (!endAt.isAfter(startAt)) {
            throw new BadRequestException("endAt은 startAt 이후여야 합니다.");
        }
    }

    /** Boolean 컬럼 보정: null -> false */
    private boolean toBoolOrFalse(Boolean v) { return v != null && v; }

    /* ========= 프로젝트 일정 ========= */

    @Override
    public ProjectCalendar scheduleProjectEvent(Long projectId, String title, String description,
                                                LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                                String rrule, String location, Long createdBy) {
        if (projectId == null) throw new BadRequestException("projectId는 필수입니다.");
        if (title == null || title.isBlank()) throw new BadRequestException("title은 필수입니다.");
        validateTimeRange(startAt, endAt);

        ProjectCalendar e = new ProjectCalendar();
        var p = new com.azure.model.project.Project(); p.setId(projectId);
        e.setProject(p);

        e.setTitle(title);
        e.setDescription(description);
        e.setStartAt(startAt);
        e.setEndAt(endAt);
        e.setAllDay(toBoolOrFalse(allDay));
        e.setRrule(rrule);
        e.setLocation(location);

        if (createdBy != null) {
            var u = new com.azure.model.user.User(); u.setId(createdBy);
            e.setCreatedBy(u);
        }

        return projectCalendarRepository.save(e);
    }

    @Override
    public ProjectCalendar updateProjectEvent(Long eventId, String title, String description,
                                              LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                              String rrule, String location, String color, Long relatedTaskId) {
        if (eventId == null) throw new BadRequestException("eventId는 필수입니다.");
        if (title == null || title.isBlank()) throw new BadRequestException("title은 필수입니다.");
        validateTimeRange(startAt, endAt);

        ProjectCalendar e = projectCalendarRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Project event not found: " + eventId));

        e.setTitle(title);
        e.setDescription(description);
        e.setStartAt(startAt);
        e.setEndAt(endAt);
        e.setAllDay(toBoolOrFalse(allDay));
        e.setRrule(rrule);
        e.setLocation(location);
        if (color != null && !color.isBlank()) e.setColor(color);

        if (relatedTaskId != null) {
            var t = new com.azure.model.task.Task(); t.setId(relatedTaskId);
            e.setRelatedTask(t);
        } else {
            e.setRelatedTask(null);
        }

        return projectCalendarRepository.save(e);
    }

    @Override
    public void deleteProjectEvent(Long eventId) {
        if (!projectCalendarRepository.existsById(eventId)) {
            throw new NotFoundException("Project event not found: " + eventId);
        }
        // 참석자/리마인더에 FK 제약이 있다면 여기서 먼저 정리할 수도 있음
        projectCalendarRepository.deleteById(eventId);
    }

    /* ========= 개인 일정 ========= */

    @Override
    public PersonalCalendar schedulePersonalEvent(Long creatorId, String title, String description,
                                                  LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                                  String rrule, String location) {
        if (title == null || title.isBlank()) throw new BadRequestException("title은 필수입니다.");
        validateTimeRange(startAt, endAt);

        PersonalCalendar e = new PersonalCalendar();
        e.setTitle(title);
        e.setDescription(description);
        e.setStartAt(startAt);
        e.setEndAt(endAt);
        e.setAllDay(toBoolOrFalse(allDay));
        e.setRrule(rrule);
        e.setLocation(location);

        if (creatorId != null) {
            var u = new com.azure.model.user.User(); u.setId(creatorId);
            e.setCreatedBy(u);
        }

        // DB DEFAULT에 의존하지 않고 명시
        e.setIsDone(false);

        return personalCalendarRepository.save(e);
    }

    @Override
    public void deletePersonalEvent(Long eventId) {
        if (!personalCalendarRepository.existsById(eventId)) {
            throw new NotFoundException("Personal event not found: " + eventId);
        }
        personalCalendarRepository.deleteById(eventId);
    }

    /* ========= 목록 ========= */

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectCalendar> listProjectEvents(Long projectId, Pageable pageable) {
        if (projectId == null) throw new BadRequestException("projectId는 필수입니다.");
        return projectCalendarRepository.findByProjectId(projectId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonalCalendar> listPersonalEvents(Long userId, Pageable pageable) {
        if (userId == null) throw new BadRequestException("userId는 필수입니다.");
        return personalCalendarRepository.findByCreatedBy_Id(userId, pageable);
    }

    /* ========= 리마인더 ========= */

    @Override
    public void addReminderForProjectEvent(Long eventId, Long userId, int minutesBefore, String method) {
        if (eventId == null || userId == null) throw new BadRequestException("eventId/userId는 필수입니다.");
        if (minutesBefore < 0) throw new BadRequestException("minutesBefore는 0 이상이어야 합니다.");

        Reminder r = new Reminder();
        var e = new ProjectCalendar(); e.setId(eventId);
        var u = new com.azure.model.user.User(); u.setId(userId);

        r.setProjectEvent(e);
        r.setUser(u);
        r.setMinutesBefore(minutesBefore);
        r.setMethod(method);

        reminderRepository.save(r);
    }

    @Override
    public void addReminderForPersonalEvent(Long eventId, Long userId, int minutesBefore, String method) {
        if (eventId == null || userId == null) throw new BadRequestException("eventId/userId는 필수입니다.");
        if (minutesBefore < 0) throw new BadRequestException("minutesBefore는 0 이상이어야 합니다.");

        Reminder r = new Reminder();
        var e = new PersonalCalendar(); e.setId(eventId);
        var u = new com.azure.model.user.User(); u.setId(userId);

        r.setPersonalEvent(e);
        r.setUser(u);
        r.setMinutesBefore(minutesBefore);
        r.setMethod(method);

        reminderRepository.save(r);
    }

    /* ========= 개인 일정 완료 ========= */

    @Override
    public PersonalCalendar setPersonalEventDone(Long eventId, boolean done) {
        PersonalCalendar e = personalCalendarRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Personal event not found: " + eventId));

        e.setIsDone(done);
        return personalCalendarRepository.save(e);
    }

    /* ========= 참석자 ========= */

    @Override
    @Transactional(readOnly = true)
    public List<EventAttendee> listEventAttendees(Long eventId) {
        if (eventId == null) throw new BadRequestException("eventId는 필수입니다.");
        return eventAttendeeRepository.findByEvent_Id(eventId);
    }

    @Override
    public EventAttendee addEventAttendee(Long eventId, Long userId, String role, String response) {
        if (eventId == null || userId == null) throw new BadRequestException("eventId/userId는 필수입니다.");

        projectCalendarRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        if (eventAttendeeRepository.existsByEvent_IdAndUser_Id(eventId, userId)) {
            return eventAttendeeRepository.findByEvent_Id(eventId).stream()
                    .filter(a -> a.getUser().getId().equals(userId))
                    .findFirst().orElse(null);
        }

        EventAttendee a = new EventAttendee();
        a.setEvent(em.getReference(ProjectCalendar.class, eventId));
        a.setUser(em.getReference(User.class, userId)); // ★ 여기!
        a.setRole(role);
        a.setResponse(response);

        return eventAttendeeRepository.save(a);
    }

    @Override
    public void removeEventAttendee(Long eventId, Long userId) {
        if (eventId == null || userId == null) throw new BadRequestException("eventId/userId는 필수입니다.");
        eventAttendeeRepository.deleteByEvent_IdAndUser_Id(eventId, userId);
    }

    @Override
    public List<EventAttendee> replaceEventAttendees(Long eventId, List<Long> userIds) {
        if (eventId == null) throw new BadRequestException("eventId는 필수입니다.");
        if (userIds == null) userIds = List.of();

        List<EventAttendee> current = eventAttendeeRepository.findByEvent_Id(eventId);
        Set<Long> keep = new HashSet<>(userIds);

        // 제거
        for (EventAttendee ea : current) {
            if (!keep.contains(ea.getUser().getId())) {
                eventAttendeeRepository.delete(ea);
            }
        }

        // 추가
        for (Long uid : userIds) {
            if (!eventAttendeeRepository.existsByEvent_IdAndUser_Id(eventId, uid)) {
                addEventAttendee(eventId, uid, "MEMBER", null);
            }
        }

        return eventAttendeeRepository.findByEvent_Id(eventId);
    }
}
