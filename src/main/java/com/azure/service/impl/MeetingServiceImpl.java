package com.azure.service.impl;

import com.azure.config.WebUserAdvice;
import com.azure.model.Organization;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.meeting.Meeting;
import com.azure.model.project.Project;
import com.azure.model.user.User;
import com.azure.repository.MeetingRepository;
import com.azure.repository.ProjectCalendarRepository;
import com.azure.repository.ProjectRepository;
import com.azure.repository.UserRepository;
import com.azure.service.MeetingService;
import com.azure.websocket.error.NotFoundException;
import com.azure.websocket.error.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final ProjectRepository projectRepository;
    private final ProjectCalendarRepository calendarRepository;
    private final UserRepository userRepository;

    private static final String PERMANENT_MEETING_ROOM_TITLE_FORMAT = "[%s] 상시 회의실";

    @Override
    public Meeting startMeeting(Long organizationId, Long projectId) {
        // 1) 세션에서 현재 사용자 ID 확보
        Long uid = WebUserAdvice.currentUserId();
        if (uid == null) throw new UnauthorizedException();

        // 2) 프로젝트 로드
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("project"));

        // 3) 대표 캘린더 이벤트 생성 (중복 방지 로직 없이 단순 생성; created_by 필수 세팅)
        User creator = userRepository.getReferenceById(uid);

        ProjectCalendar ev = new ProjectCalendar();
        ev.setProject(project);
        ev.setTitle(String.format(PERMANENT_MEETING_ROOM_TITLE_FORMAT, project.getName()));
        ev.setStartAt(LocalDateTime.now());
        ev.setEndAt(LocalDateTime.now());
        ev.setAllDay(false);
        ev.setCreatedBy(creator); // ⬅️ created_by NOT NULL 해결
        ev = calendarRepository.save(ev);

        // 4) 회의 엔티티 저장 (Meeting에는 startedBy 필드가 없음)
        Meeting m = new Meeting();
        if (organizationId != null) {
            Organization org = new Organization();
            org.setId(organizationId);
            m.setOrganization(org);
        }
        m.setProject(project);
        m.setStartedAt(LocalDateTime.now());
        m.setEvent(ev); // Meeting 엔티티에 event 필드가 있음

        return meetingRepository.save(m);
    }

    @Override
    public Meeting endMeeting(Long meetingId) {
        Meeting m = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("meeting"));
        if (m.getEndedAt() == null) {
            m.setEndedAt(LocalDateTime.now());
        }
        return meetingRepository.save(m);
    }
}
