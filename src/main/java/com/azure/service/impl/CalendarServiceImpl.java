package com.azure.service.impl;

import com.azure.model.calendar.PersonalCalendar;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.reminder.Reminder;
import com.azure.repository.PersonalCalendarRepository;
import com.azure.repository.ProjectCalendarRepository;
import com.azure.repository.ReminderRepository;
import com.azure.service.CalendarService;
import com.azure.service.exception.BadRequestException;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


/**
 * 캘린더 도메인 서비스 구현체
 *
 * <h2>트랜잭션 정책</h2>
 * <ul>
 *   <li>클래스 레벨 @Transactional: 쓰기 메서드는 기본 트랜잭션</li>
 *   <li>조회 메서드는 @Transactional(readOnly=true)로 최적화</li>
 * </ul>
 *
 * <h2>스키마 대응</h2>
 * <ul>
 *   <li><b>is_done</b>(TINYINT(1) NOT NULL DEFAULT 0): 생성 시 <b>false를 명시</b>하여 DB DEFAULT 미적용 문제 회피</li>
 *   <li><b>all_day</b>(TINYINT(1) DEFAULT 0): null로 들어오면 false로 강제하여 일관성 유지</li>
 *   <li><b>created_at/updated_at</b>: DB 자동 관리 컬럼 → 코드에서 설정/수정하지 않음</li>
 *   <li><b>created_by FK (NO ACTION)</b>: 해당 사용자가 참조되면 삭제 불가 → 상위 정책에서 제약 고려</li>
 * </ul>
 *
 * <h2>페이징 전략</h2>
 * <ul>
 *   <li>초기에는 List→Page(PageImpl)로 감싸는 임시 페이징 허용</li>
 *   <li>운영 전환 시 레포지토리에 <code>findBy...(..., Pageable)</code> 추가하여 DB 레벨 페이징으로 교체</li>
 * </ul>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final ProjectCalendarRepository projectCalendarRepository;
    private final PersonalCalendarRepository personalCalendarRepository;
    private final ReminderRepository reminderRepository;

    // ──────────────────────────────── 내부 검증 유틸 ────────────────────────────────

    /** 시작/종료 시간 검증 (둘 다 필수, end > start) */
    private void validateTimeRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new BadRequestException("startAt과 endAt은 필수입니다.");
        }
        if (!endAt.isAfter(startAt)) {
            throw new BadRequestException("endAt은 startAt 이후여야 합니다.");
        }
    }

    /** Boolean 컬럼(ALL_DAY 등) 기본값 보정: null → false */
    private boolean toBoolOrFalse(Boolean value) {
        return value != null && value;
    }

    // ──────────────────────────────── 프로젝트 일정 ────────────────────────────────

    @Override
    public ProjectCalendar scheduleProjectEvent(Long projectId, String title, String description,
                                                LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                                String rrule, String location, Long createdBy) {
        // 1) 입력 검증
        if (projectId == null) throw new BadRequestException("projectId는 필수입니다.");
        if (title == null || title.isBlank()) throw new BadRequestException("title은 필수입니다.");
        validateTimeRange(startAt, endAt);

        // 2) 엔티티 조립 (연관관계는 ID만 세팅해도 FK 매핑됨)
        ProjectCalendar e = new ProjectCalendar();
        e.setProject(new com.azure.model.project.Project());
        e.getProject().setId(projectId);

        e.setTitle(title);
        e.setDescription(description);
        e.setStartAt(startAt);
        e.setEndAt(endAt);
        e.setAllDay(toBoolOrFalse(allDay)); // null이면 false
        e.setRrule(rrule);
        e.setLocation(location);

        if (createdBy != null) {
            e.setCreatedBy(new com.azure.model.user.User());
            e.getCreatedBy().setId(createdBy);
        }

        // 3) 저장
        return projectCalendarRepository.save(e);
    }

    // ──────────────────────────────── 개인 일정 ────────────────────────────────

    @Override
    public PersonalCalendar schedulePersonalEvent(Long creatorId, String title, String description,
                                                  LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                                  String rrule, String location) {
        // 1) 입력 검증
        if (title == null || title.isBlank()) throw new BadRequestException("title은 필수입니다.");
        validateTimeRange(startAt, endAt);

        // 2) 엔티티 조립
        PersonalCalendar e = new PersonalCalendar();
        e.setTitle(title);
        e.setDescription(description);
        e.setStartAt(startAt);
        e.setEndAt(endAt);
        e.setAllDay(toBoolOrFalse(allDay)); // null이면 false
        e.setRrule(rrule);
        e.setLocation(location);
        if (creatorId != null) {
            e.setCreatedBy(new com.azure.model.user.User());
            e.getCreatedBy().setId(creatorId);
        }

        // ★ 핵심: is_done 기본 false 명시 (DB DEFAULT에만 의존하지 않음)
        e.setIsDone(false);

        // 3) 저장
        return personalCalendarRepository.save(e);
    }

    // ──────────────────────────────── 목록 ────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectCalendar> listProjectEvents(Long projectId, Pageable pageable) {
        if (projectId == null) throw new BadRequestException("projectId는 필수입니다.");
        // 변경: 레포지토리 페이징 메서드 직접 호출(성능/간결성 향상)
        return projectCalendarRepository.findByProjectId(projectId, pageable); //
    }


    @Override
    @Transactional(readOnly = true)
    public Page<PersonalCalendar> listPersonalEvents(Long userId, Pageable pageable) {
        if (userId == null) throw new BadRequestException("userId는 필수입니다.");
        // ★ 변경: "내 일정만" 사용자 기준으로 페이징 조회
        return personalCalendarRepository.findByCreatedBy_Id(userId, pageable); // ★
    }

    // ──────────────────────────────── 삭제 ────────────────────────────────

    @Override
    public void deleteProjectEvent(Long eventId) {
        if (!projectCalendarRepository.existsById(eventId)) {
            throw new NotFoundException("Project event not found: " + eventId);
        }
        projectCalendarRepository.deleteById(eventId);
    }

    @Override
    public void deletePersonalEvent(Long eventId) {
        if (!personalCalendarRepository.existsById(eventId)) {
            throw new NotFoundException("Personal event not found: " + eventId);
        }
        personalCalendarRepository.deleteById(eventId);
    }

    // ──────────────────────────────── 리마인더 ────────────────────────────────

    @Override
    public void addReminderForProjectEvent(Long eventId, Long userId, int minutesBefore, String method) {
        if (eventId == null || userId == null) throw new BadRequestException("eventId/userId는 필수입니다.");
        if (minutesBefore < 0) throw new BadRequestException("minutesBefore는 0 이상이어야 합니다.");

        Reminder r = new Reminder();
        r.setProjectEvent(new ProjectCalendar()); r.getProjectEvent().setId(eventId);
        r.setUser(new com.azure.model.user.User()); r.getUser().setId(userId);
        r.setMinutesBefore(minutesBefore);
        r.setMethod(method);

        reminderRepository.save(r);
    }

    @Override
    public void addReminderForPersonalEvent(Long eventId, Long userId, int minutesBefore, String method) {
        if (eventId == null || userId == null) throw new BadRequestException("eventId/userId는 필수입니다.");
        if (minutesBefore < 0) throw new BadRequestException("minutesBefore는 0 이상이어야 합니다.");

        Reminder r = new Reminder();
        r.setPersonalEvent(new PersonalCalendar()); r.getPersonalEvent().setId(eventId);
        r.setUser(new com.azure.model.user.User()); r.getUser().setId(userId);
        r.setMinutesBefore(minutesBefore);
        r.setMethod(method);

        reminderRepository.save(r);
    }

    // ──────────────────────────────── 개인 일정 완료/미완료 ────────────────────────────────

    @Override
    public PersonalCalendar setPersonalEventDone(Long eventId, boolean done) {
        PersonalCalendar e = personalCalendarRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Personal event not found: " + eventId));

        e.setIsDone(done);
        return personalCalendarRepository.save(e);
    }
}
