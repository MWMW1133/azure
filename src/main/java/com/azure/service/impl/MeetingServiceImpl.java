// src/main/java/com/azure/service/impl/MeetingServiceImpl.java
package com.azure.service.impl;

import com.azure.dto.MeetingDTO;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.meeting.Meeting;
import com.azure.repository.MeetingRepository;
import com.azure.repository.OrganizationRepository;
import com.azure.repository.ProjectCalendarRepository; // ← 이벤트 조회
import com.azure.repository.ProjectRepository;
import com.azure.service.MeetingService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.azure.event.MeetingStartedEvent;
import com.azure.event.MeetingEndedEvent;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectCalendarRepository projectCalendarRepository; // ← add
    private final ApplicationEventPublisher publisher;

    @Override
    public Meeting startMeeting(Long eventId, Long organizationId, Long projectId, LocalDateTime startedAt) {
        ProjectCalendar event = projectCalendarRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        var org = (organizationId != null)
                ? organizationRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found: " + organizationId))
                : null;

        var project = (projectId != null)
                ? projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId))
                : null;

        Meeting m = new Meeting();
        m.setEvent(event);                   // ★ 필수
        m.setOrganization(org);              // nullable
        m.setProject(project);               // nullable
        m.setStartedAt(startedAt != null ? startedAt : LocalDateTime.now());

        Meeting saved = meetingRepository.save(m);
        publisher.publishEvent(new MeetingStartedEvent(saved));
        return saved;
    }

    @Override
    public Meeting endMeeting(Long meetingId, LocalDateTime endedAt) {
        Meeting m = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("Meeting not found: " + meetingId));
        if (m.getEndedAt() != null) {
            throw new IllegalStateException("Meeting already ended at " + m.getEndedAt());
        }
        m.setEndedAt(endedAt != null ? endedAt : LocalDateTime.now());
        Meeting saved = meetingRepository.save(m);
        publisher.publishEvent(new MeetingEndedEvent(saved));
        return saved;
    }

    @Override
    public MeetingDTO create(MeetingDTO dto) {
        if (dto.getEventId() == null) throw new NotFoundException("eventId is required");

        ProjectCalendar event = projectCalendarRepository.findById(dto.getEventId())
                .orElseThrow(() -> new NotFoundException("Event not found"));

        var org = (dto.getOrganizationId() != null)
                ? organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> new NotFoundException("Organization not found"))
                : null;

        var project = (dto.getProjectId() != null)
                ? projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new NotFoundException("Project not found"))
                : null;

        Meeting m = new Meeting();
        m.setEvent(event);
        m.setOrganization(org);
        m.setProject(project);
        m.setStartedAt(dto.getStartedAt());
        m.setEndedAt(dto.getEndedAt());

        return toDto(meetingRepository.save(m));
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingDTO get(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Meeting not found: " + meetingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeetingDTO> listByOrganization(Long organizationId, Pageable pageable) {
        return meetingRepository.findByOrganization_Id(organizationId, pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeetingDTO> listByProject(Long projectId, Pageable pageable) {
        return meetingRepository.findByProject_Id(projectId, pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeetingDTO> listByOrganizationAndProject(Long organizationId, Long projectId, Pageable pageable) {
        return meetingRepository.findByOrganization_IdAndProject_Id(organizationId, projectId, pageable).map(this::toDto);
    }

    private MeetingDTO toDto(Meeting m) {
        MeetingDTO dto = new MeetingDTO();
        dto.setId(m.getId());

        // event
        if (m.getEvent() != null) {
            dto.setEventId(m.getEvent().getId());
            // meetingName/Description은 event의 title/desc에서 끌어오는 게 일반적
            // dto.setMeetingName(m.getEvent().getTitle());
            // dto.setMeetingDescription(m.getEvent().getDescription());
        }

        // org/project (NULL 허용)
        if (m.getOrganization() != null) {
            dto.setOrganizationId(m.getOrganization().getId());
            dto.setOrganizationName(m.getOrganization().getName());
        }
        if (m.getProject() != null) {
            dto.setProjectId(m.getProject().getId());
            dto.setProjectName(m.getProject().getName());
        }

        dto.setStartedAt(m.getStartedAt());
        dto.setEndedAt(m.getEndedAt());

        if (m.getRecordingFile() != null) {
            dto.setRecordingFileUrl("/files/" + m.getRecordingFile().getId());
        }
        return dto;
    }
}
