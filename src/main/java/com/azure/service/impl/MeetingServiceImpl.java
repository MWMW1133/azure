package com.azure.service.impl;

import com.azure.model.Organization;
import com.azure.model.project.Project;
import com.azure.model.meeting.Meeting;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.user.User;
import com.azure.repository.MeetingRepository;
import com.azure.repository.ProjectCalendarRepository;
import com.azure.repository.ProjectRepository;
import com.azure.repository.UserRepository;
import com.azure.security.SecurityUtil; // ❗️ SecurityUtil import
import com.azure.service.MeetingService;
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
    private final UserRepository userRepository; // ❗️ UserRepository 주입 확인

    private static final String PERMANENT_MEETING_ROOM_TITLE_FORMAT = "[%s] 상시 회의실";

    @Override
    public Meeting startMeeting(Long organizationId, Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        String roomTitle = String.format(PERMANENT_MEETING_ROOM_TITLE_FORMAT, project.getName());

        ProjectCalendar representativeEvent = calendarRepository.findByProjectIdAndTitle(projectId, roomTitle)
                .orElseGet(() -> {
                    // 1. SecurityUtil을 사용해서 현재 로그인한 사용자 ID를 가져옵니다.
                    Long currentUserId = SecurityUtil.getCurrentUserId();
                    if (currentUserId == null) {
                        // SecurityConfig에서 API 접근을 막아주므로 사실 이 코드는 거의 실행되지 않습니다.
                        // 하지만 안전을 위해 한 번 더 확인합니다.
                        throw new IllegalStateException("User not authenticated for creating a calendar event.");
                    }

                    User currentUser = userRepository.findById(currentUserId)
                            .orElseThrow(() -> new RuntimeException("Current user with ID " + currentUserId + " not found in DB"));

                    // 2. 새로운 캘린더 이벤트를 만들고 생성자를 설정합니다.
                    ProjectCalendar newEvent = new ProjectCalendar();
                    newEvent.setProject(project);
                    newEvent.setTitle(roomTitle);
                    newEvent.setStartAt(LocalDateTime.now());
                    newEvent.setEndAt(LocalDateTime.now());
                    newEvent.setCreatedBy(currentUser); // ✅ 문제 해결

                    return calendarRepository.save(newEvent);
                });

        Meeting newMeeting = new Meeting();
        if (organizationId != null) {
            Organization org = new Organization();
            org.setId(organizationId);
            newMeeting.setOrganization(org);
        }
        newMeeting.setProject(project);
        newMeeting.setStartedAt(LocalDateTime.now());
        newMeeting.setEvent(representativeEvent);

        return meetingRepository.save(newMeeting);
    }

    @Override
    public Meeting endMeeting(Long meetingId) {
        Meeting m = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found with id: " + meetingId));
        if (m.getEndedAt() == null) {
            m.setEndedAt(LocalDateTime.now());
        }
        return meetingRepository.save(m);
    }
}
