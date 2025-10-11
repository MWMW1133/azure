package com.azure.service;

import com.azure.model.calendar.EventAttendee;
import com.azure.model.calendar.PersonalCalendar;
import com.azure.model.calendar.ProjectCalendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 캘린더 도메인 서비스 (프로젝트 일정 + 개인 일정)
 */
public interface CalendarService {

    /** 프로젝트 일정 생성 */
    ProjectCalendar scheduleProjectEvent(Long projectId, String title, String description,
                                         LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                         String rrule, String location, Long createdBy);

    /** ✅ 프로젝트 일정 수정 */
    ProjectCalendar updateProjectEvent(Long eventId, String title, String description,
                                       LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                       String rrule, String location, String color, Long relatedTaskId);

    /** 프로젝트 일정 삭제 */
    void deleteProjectEvent(Long eventId);

    /** 개인 일정 생성 */
    PersonalCalendar schedulePersonalEvent(Long creatorId, String title, String description,
                                           LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                           String rrule, String location);

    /** 개인 일정 삭제 */
    void deletePersonalEvent(Long eventId);

    /** 프로젝트 일정 목록(페이징) */
    Page<ProjectCalendar> listProjectEvents(Long projectId, Pageable pageable);

    /** 개인 일정 목록(페이징) - 사용자 기준 */
    Page<PersonalCalendar> listPersonalEvents(Long userId, Pageable pageable);

    /** 프로젝트 일정 리마인더 추가 */
    void addReminderForProjectEvent(Long eventId, Long userId, int minutesBefore, String method);

    /** 개인 일정 리마인더 추가 */
    void addReminderForPersonalEvent(Long eventId, Long userId, int minutesBefore, String method);

    /** 개인 일정 완료/미완료 설정 */
    PersonalCalendar setPersonalEventDone(Long eventId, boolean done);

    /* =========================
       참석자 (project event)
       ========================= */

    /** 특정 이벤트의 참석자 목록 */
    List<EventAttendee> listEventAttendees(Long eventId);

    /** 참석자 한 명 추가 (중복시 무시 또는 기존 반환) */
    EventAttendee addEventAttendee(Long eventId, Long userId, String role, String response);

    /** 참석자 한 명 제거 */
    void removeEventAttendee(Long eventId, Long userId);

    /** 참석자 일괄 설정(교체): 전달된 userId 목록만 남기고 나머지는 제거 */
    List<EventAttendee> replaceEventAttendees(Long eventId, List<Long> userIds);
}
