package com.azure.service;

import com.azure.model.calendar.PersonalCalendar;
import com.azure.model.calendar.ProjectCalendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

/**
 * 프로젝트/개인 캘린더 일정 관리 기능을 제공한다.
 * rrule(반복 규칙) 문자열은 프론트/컨트롤러에서 생성하여 전달한다.
 */
public interface CalendarService {
    /** 프로젝트 일정 생성. */
    ProjectCalendar scheduleProjectEvent(Long projectId, String title, String description,
                                         LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                         String rrule, String location, Long createdBy);

    /** 개인 일정 생성. */
    PersonalCalendar schedulePersonalEvent(Long creatorId, String title, String description,
                                           LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                           String rrule, String location);

    /** 프로젝트 일정 목록(페이징). */
    Page<ProjectCalendar> listProjectEvents(Long projectId, Pageable pageable);

    /** 개인 일정 목록(페이징). */
    Page<PersonalCalendar> listPersonalEvents(Long userId, Pageable pageable);

    /** 프로젝트/개인 일정 삭제. */
    void deleteProjectEvent(Long eventId);
    void deletePersonalEvent(Long eventId);

    /** 알림(리마인더) 추가. */
    void addReminderForProjectEvent(Long eventId, Long userId, int minutesBefore, String method);
    void addReminderForPersonalEvent(Long eventId, Long userId, int minutesBefore, String method);
}
