package com.azure.service.impl;

import com.azure.model.Organization;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.meeting.Meeting;
import com.azure.model.project.Project;
import com.azure.repository.MeetingRepository;
import com.azure.repository.ProjectCalendarRepository;
import com.azure.repository.ProjectRepository;
import com.azure.service.MeetingService;
import com.azure.service.exception.BadRequestException;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private static final String ORG_MEETING_PROJECT_NAME_PREFIX = "ORG-MEETING-"; // 조직 공용 프로젝트명
    private static final String ORG_MEETING_EVENT_TITLE = "[OrgRoom]";            // 앵커 이벤트 제목

    private final MeetingRepository meetingRepository;
    private final ProjectCalendarRepository projectCalendarRepository;
    private final ProjectRepository projectRepository;

    @Override
    public Meeting startMeeting(Long eventId, Long organizationId, Long projectId, LocalDateTime startedAt) {
        if (eventId == null) throw new BadRequestException("eventId는 필수입니다.");

        // 1) 이벤트 확인
        ProjectCalendar event = projectCalendarRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Project event not found: " + eventId));

        // 2) 중복 진행 방지(같은 event에 미종료 회의 존재?)
        if (meetingRepository.existsByEvent_IdAndEndedAtIsNull(eventId)) {
            throw new BadRequestException("이미 진행 중인 회의가 있습니다.");
        }

        // 3) 조립
        Meeting m = new Meeting();
        m.setEvent(event);

        if (organizationId != null) {
            Organization org = new Organization(); org.setId(organizationId);
            m.setOrganization(org);
        }
        if (projectId != null) {
            Project p = new Project(); p.setId(projectId);
            m.setProject(p);
        }

        m.setStartedAt(startedAt != null ? startedAt : LocalDateTime.now());
        return meetingRepository.save(m);
    }

    @Override
    public Meeting endMeeting(Long meetingId, LocalDateTime endedAt) {
        Meeting m = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("Meeting not found: " + meetingId));

        LocalDateTime end = (endedAt != null ? endedAt : LocalDateTime.now());
        if (m.getStartedAt() != null && !end.isAfter(m.getStartedAt())) {
            throw new BadRequestException("endedAt은 startedAt 이후여야 합니다.");
        }
        m.setEndedAt(end);
        return meetingRepository.save(m);
    }

    // ─────────────────────────────────────────────
    // [ADD] 조직 공용 회의방 시작 (eventId 몰라도 호출 가능)
    // ─────────────────────────────────────────────
    @Override
    public Meeting startOrgRoom(Long organizationId, LocalDateTime startedAt) {
        if (organizationId == null) throw new BadRequestException("organizationId는 필수입니다.");
        Long anchorEventId = ensureOrgAnchorEvent(organizationId);
        // projectId는 고정 프로젝트의 id를 넘겨도 되고, null이어도 동작 가능(스키마상 선택)
        Long anchorProjectId = findOrgAnchorProjectId(organizationId);
        return startMeeting(anchorEventId, organizationId, anchorProjectId, startedAt);
    }

    // 조직별 "공용 회의 프로젝트" id 조회(없으면 생성)
    private Long findOrgAnchorProjectId(Long organizationId) {
        String projectName = ORG_MEETING_PROJECT_NAME_PREFIX + organizationId;
        return projectRepository.findByOrganization_IdAndName(organizationId, projectName)
                .orElseGet(() -> {
                    Project p = new Project();
                    p.setName(projectName);
                    // p.setCode(...); p.setDescription(...); 필요 시 채우기
                    // 조직 FK
                    Organization org = new Organization(); org.setId(organizationId);
                    p.setOrganization(org);
                    return projectRepository.save(p);
                })
                .getId();
    }

    // 조직별 앵커 "ProjectCalendar 이벤트" id 조회(없으면 생성)
    private Long ensureOrgAnchorEvent(Long organizationId) {
        Long projectId = findOrgAnchorProjectId(organizationId);

        return projectCalendarRepository
                .findFirstByProject_IdAndTitle(projectId, ORG_MEETING_EVENT_TITLE)
                .orElseGet(() -> {
                    ProjectCalendar e = new ProjectCalendar();
                    Project project = new Project(); project.setId(projectId);
                    e.setProject(project);
                    e.setTitle(ORG_MEETING_EVENT_TITLE);
                    // 상시룸이므로 시간은 의미 없음(필드 NOT NULL이면 기본값)
                    e.setAllDay(true);
                    e.setDescription("Organization shared meeting room anchor");
                    // start/end NULL 허용 스키마면 생략; 아니면 현재 시각/미래시각 넣기
                    return projectCalendarRepository.save(e);
                })
                .getId();
    }
}
