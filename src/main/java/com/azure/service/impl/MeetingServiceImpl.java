package com.azure.service.impl;

import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.meeting.Meeting;
import com.azure.repository.MeetingRepository;
import com.azure.repository.ProjectCalendarRepository;
import com.azure.service.MeetingService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 회의 서비스 구현.
 * - 프로젝트 일정과 연결하여 회의 시작/종료 처리
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final ProjectCalendarRepository projectCalendarRepository;

    @Override
    public Meeting startMeeting(Long projectEventId, Long projectId, LocalDateTime startedAt) {
        ProjectCalendar event = projectCalendarRepository.findById(projectEventId)
                .orElseThrow(() -> new NotFoundException("Project event not found: " + projectEventId));
        Meeting m = new Meeting();
        m.setEvent(event);
        m.setProject(new com.azure.model.project.Project()); m.getProject().setId(projectId);
        m.setStartedAt(startedAt != null ? startedAt : LocalDateTime.now());
        return meetingRepository.save(m);
    }

    @Override
    public Meeting endMeeting(Long meetingId, LocalDateTime endedAt) {
        Meeting m = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("Meeting not found: " + meetingId));
        m.setEndedAt(endedAt != null ? endedAt : LocalDateTime.now());
        return meetingRepository.save(m);
    }
}
