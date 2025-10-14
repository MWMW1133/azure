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

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("project"));

        String roomTitle = String.format(PERMANENT_MEETING_ROOM_TITLE_FORMAT, project.getName());

        // 1. 기존에 만들어 둔 '상시 회의실' 대표 일정이 있는지 먼저 찾습니다.
        ProjectCalendar representativeEvent = calendarRepository.findByProjectIdAndTitle(projectId, roomTitle)
                .orElseGet(() -> {
                    // 2. 없을 때만 새로 생성합니다.
                    System.out.println("대표 일정이 없어 새로 생성합니다: " + roomTitle);

                    Long currentUserId = WebUserAdvice.currentUserId();
                    if (currentUserId == null) {
                        throw new UnauthorizedException("User not authenticated for creating a calendar event.");
                    }

                    User currentUser = userRepository.findById(currentUserId)
                            .orElseThrow(() -> new NotFoundException("user"));

                    ProjectCalendar newEvent = new ProjectCalendar();
                    newEvent.setProject(project);
                    newEvent.setTitle(roomTitle);
                    newEvent.setStartAt(LocalDateTime.now());
                    newEvent.setEndAt(LocalDateTime.now());
                    newEvent.setCreatedBy(currentUser);

                    return calendarRepository.save(newEvent);
                });

        // 3. 새로운 회의(Meeting) 기록을 만들고, 찾거나 생성한 대표 일정과 연결합니다.
        Meeting newMeeting = new Meeting();
        if (organizationId != null) {
            var o = new Organization();
            o.setId(organizationId);
            newMeeting.setOrganization(o);
        }
        newMeeting.setProject(project);
        newMeeting.setStartedAt(LocalDateTime.now());
        newMeeting.setEvent(representativeEvent);

        return meetingRepository.save(newMeeting);
    }

    @Override
    public Meeting endMeeting(Long meetingId) {
        Meeting m = meetingRepository.findByIdWithEvent(meetingId)
                .orElseThrow(() -> new NotFoundException("meeting"));

        if (m.getEndedAt() == null) {
            m.setEndedAt(LocalDateTime.now());
        }
        return meetingRepository.save(m);
    }
}

