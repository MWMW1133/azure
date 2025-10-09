package com.azure.service.impl;

import com.azure.dto.MeetingDTO;
import com.azure.model.meeting.Meeting;
import com.azure.repository.MeetingRepository;
import com.azure.repository.OrganizationRepository;
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
    private final ApplicationEventPublisher publisher;
    @Override
    public Meeting startMeeting(Long organizationId, Long projectId, LocalDateTime startedAt) {
        var org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found: " + organizationId));
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        Meeting m = new Meeting();
        m.setOrganization(org);
        m.setProject(project);
        m.setStartedAt(startedAt != null ? startedAt : LocalDateTime.now());

        Meeting saved = meetingRepository.save(m);

        // 📢 회의 시작 이벤트 발행
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

        // 📢 회의 종료 이벤트 발행
        publisher.publishEvent(new MeetingEndedEvent(saved));

        return saved;
    }

    @Override
    public MeetingDTO create(MeetingDTO dto) {
        var org = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> new NotFoundException("Organization not found"));
        var project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new NotFoundException("Project not found"));

        Meeting m = new Meeting();
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
        return meetingRepository.findByOrganization_Id(organizationId, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeetingDTO> listByProject(Long projectId, Pageable pageable) {
        return meetingRepository.findByProject_Id(projectId, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeetingDTO> listByOrganizationAndProject(Long organizationId, Long projectId, Pageable pageable) {
        return meetingRepository.findByOrganization_IdAndProject_Id(organizationId, projectId, pageable)
                .map(this::toDto);
    }

    private MeetingDTO toDto(Meeting m) {
        MeetingDTO dto = new MeetingDTO();
        dto.setId(m.getId());
        dto.setOrganizationId(m.getOrganization().getId());
        dto.setOrganizationName(m.getOrganization().getName());
        dto.setProjectId(m.getProject().getId());
        dto.setProjectName(m.getProject().getName());
        dto.setStartedAt(m.getStartedAt());
        dto.setEndedAt(m.getEndedAt());
        if (m.getRecordingFile() != null) {
            dto.setRecordingFileUrl("/files/" + m.getRecordingFile().getId());
        }
        return dto;
    }
}
