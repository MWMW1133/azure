package com.azure.service.impl;

import com.azure.model.Organization;
import com.azure.model.project.Project;
import com.azure.model.meeting.Meeting;
import com.azure.model.calendar.ProjectCalendar; // ❗️ Import 추가
import com.azure.repository.MeetingRepository;
import com.azure.repository.ProjectCalendarRepository; // ❗️ Repository 주입 추가
import com.azure.repository.ProjectRepository;       // ❗️ Repository 주입 추가
import com.azure.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import com.azure.model.user.User;
import com.azure.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // UserDetails import 확인

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final ProjectRepository projectRepository; // ❗️ 주입 추가
    private final ProjectCalendarRepository calendarRepository; // ❗️ 주입 추가
    private final UserRepository userRepository; // ❗️ UserRepository 주입 추가
    private static final String PERMANENT_MEETING_ROOM_TITLE_FORMAT = "[%s] 상시 회의실";


    @Override
    public Meeting startMeeting(Long organizationId, Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        String roomTitle = String.format(PERMANENT_MEETING_ROOM_TITLE_FORMAT, project.getName());

        ProjectCalendar representativeEvent = calendarRepository.findByProjectIdAndTitle(projectId, roomTitle)
                .orElseGet(() -> {
                    // 👇 orElseGet 블록 안을 수정합니다.

                    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String username;

                    // ❗️ principal의 타입에 따라 다르게 처리하도록 수정
                    if (principal instanceof UserDetails) {
                        username = ((UserDetails) principal).getUsername();
                    } else {
                        username = principal.toString();
                    }

                    User currentUser = userRepository.findByLoginId(username)
                            .orElseThrow(() -> new RuntimeException("Current user not found: " + username));

                    ProjectCalendar newEvent = new ProjectCalendar();
                    newEvent.setProject(project);
                    newEvent.setTitle(roomTitle);
                    newEvent.setStartAt(LocalDateTime.now());
                    newEvent.setEndAt(LocalDateTime.now());
                    newEvent.setCreatedBy(currentUser);

                    return calendarRepository.save(newEvent);
                });

        // ... 나머지 코드는 동일 ...
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
        Meeting m = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found with id: " + meetingId));
        if (m.getEndedAt() == null) m.setEndedAt(LocalDateTime.now());
        return meetingRepository.save(m);
    }
}