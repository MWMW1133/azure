package com.azure.service.impl;

import com.azure.model.calendar.PersonalCalendar;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.reminder.Reminder;
import com.azure.repository.PersonalCalendarRepository;
import com.azure.repository.ProjectCalendarRepository;
import com.azure.repository.ReminderRepository;
import com.azure.service.CalendarService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 캘린더 서비스 구현.
 * - 리스트 페이징은 임시로 PageImpl 사용(레포에 페이징 쿼리 추가 시 교체)
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final ProjectCalendarRepository projectCalendarRepository;
    private final PersonalCalendarRepository personalCalendarRepository;
    private final ReminderRepository reminderRepository;

    @Override
    public ProjectCalendar scheduleProjectEvent(Long projectId, String title, String description,
                                                LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                                String rrule, String location, Long createdBy) {
        ProjectCalendar e = new ProjectCalendar();
        e.setProject(new com.azure.model.project.Project()); e.getProject().setId(projectId);
        e.setTitle(title); e.setDescription(description); e.setStartAt(startAt); e.setEndAt(endAt);
        e.setAllDay(allDay); e.setRrule(rrule); e.setLocation(location);
        if (createdBy != null) { e.setCreatedBy(new com.azure.model.user.User()); e.getCreatedBy().setId(createdBy); }
        return projectCalendarRepository.save(e);
    }

    @Override
    public PersonalCalendar schedulePersonalEvent(Long creatorId, String title, String description,
                                                  LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                                  String rrule, String location) {
        PersonalCalendar e = new PersonalCalendar();
        e.setTitle(title); e.setDescription(description); e.setStartAt(startAt); e.setEndAt(endAt);
        e.setAllDay(allDay); e.setRrule(rrule); e.setLocation(location);
        if (creatorId != null) { e.setCreatedBy(new com.azure.model.user.User()); e.getCreatedBy().setId(creatorId); }
        return personalCalendarRepository.save(e);
    }

    @Override @Transactional(readOnly = true)
    public Page<ProjectCalendar> listProjectEvents(Long projectId, Pageable pageable) {
        List<ProjectCalendar> all = projectCalendarRepository.findByProjectId(projectId);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<ProjectCalendar> content = (start > end) ? List.of() : all.subList(start, end);
        return new PageImpl<>(content, pageable, all.size());
    }

    @Override @Transactional(readOnly = true)
    public Page<PersonalCalendar> listPersonalEvents(Long userId, Pageable pageable) {
        // TODO: 레포에 findByCreatedBy_Id(userId, Pageable) 추가 시 교체
        return personalCalendarRepository.findAll(pageable);
    }

    @Override
    public void deleteProjectEvent(Long eventId) { projectCalendarRepository.deleteById(eventId); }

    @Override
    public void deletePersonalEvent(Long eventId) { personalCalendarRepository.deleteById(eventId); }

    @Override
    public void addReminderForProjectEvent(Long eventId, Long userId, int minutesBefore, String method) {
        Reminder r = new Reminder();
        r.setProjectEvent(new ProjectCalendar()); r.getProjectEvent().setId(eventId);
        r.setUser(new com.azure.model.user.User()); r.getUser().setId(userId);
        r.setMinutesBefore(minutesBefore); r.setMethod(method);
        reminderRepository.save(r);
    }

    @Override
    public void addReminderForPersonalEvent(Long eventId, Long userId, int minutesBefore, String method) {
        Reminder r = new Reminder();
        r.setPersonalEvent(new PersonalCalendar()); r.getPersonalEvent().setId(eventId);
        r.setUser(new com.azure.model.user.User()); r.getUser().setId(userId);
        r.setMinutesBefore(minutesBefore); r.setMethod(method);
        reminderRepository.save(r);
    }
}
