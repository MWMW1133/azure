package com.azure.service.impl;

import com.azure.config.WebUserAdvice;
import com.azure.dto.MeetingDTO; // ✅ DTO 임포트
import com.azure.model.Organization;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.meeting.Meeting;
import com.azure.model.enums.MeetingStatus; // ✅ Status Enum 임포트
import com.azure.model.project.Project;
import com.azure.model.user.User;
import com.azure.repository.MeetingRepository;
import com.azure.repository.ProjectCalendarRepository;
import com.azure.repository.ProjectRepository;
import com.azure.repository.UserRepository;
import com.azure.service.MeetingService;
import com.azure.websocket.error.NotFoundException;
import com.azure.websocket.error.UnauthorizedException;
import jakarta.persistence.EntityNotFoundException; // ✅ JPA 예외 사용 권장
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final ProjectRepository projectRepository;
    private final ProjectCalendarRepository calendarRepository;
    private final UserRepository userRepository;

    @Value("${aws.s3.public-base-url}")
    private String s3BaseUrl;

    private static final String PERMANENT_MEETING_ROOM_TITLE_FORMAT = "[%s] 상시 회의실";

    @Override
    public MeetingDTO startMeeting(Long organizationId, Long projectId) { // ✅ 반환 타입 MeetingDTO로 변경

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("project"));

        String roomTitle = String.format(PERMANENT_MEETING_ROOM_TITLE_FORMAT, project.getName());
        List<ProjectCalendar> existingEvents = calendarRepository.findByProjectIdAndTitle(projectId, roomTitle);

        ProjectCalendar representativeEvent;
        if (existingEvents.isEmpty()) {
            Long currentUserId = WebUserAdvice.currentUserId();
            if (currentUserId == null) {
                throw new UnauthorizedException();
            }
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new NotFoundException("user"));

            ProjectCalendar newEvent = new ProjectCalendar();
            newEvent.setProject(project);
            newEvent.setTitle(roomTitle);
            newEvent.setStartAt(LocalDateTime.now());
            newEvent.setEndAt(LocalDateTime.now());
            newEvent.setCreatedBy(currentUser);

            representativeEvent = calendarRepository.save(newEvent);
        } else {
            representativeEvent = existingEvents.get(0);
        }

        Meeting newMeeting = new Meeting();
        if (organizationId != null) {
            var o = new Organization();
            o.setId(organizationId);
            newMeeting.setOrganization(o);
        }
        newMeeting.setProject(project);
        newMeeting.setStartedAt(LocalDateTime.now());
        newMeeting.setEvent(representativeEvent);
        newMeeting.setStatus(MeetingStatus.RECORDING); // ✅ 시작 시 상태를 '녹음중'으로 설정

        Meeting savedMeeting = meetingRepository.save(newMeeting);

        // ✅ 엔티티를 DTO로 변환하여 반환
        return MeetingDTO.fromEntity(savedMeeting, s3BaseUrl);
    }

    @Override
    public MeetingDTO endMeeting(Long meetingId) { // ✅ 반환 타입 MeetingDTO로 변경
        // findByIdWithEvent 대신 일반 findById 사용 권장 (이후에 DTO로 변환할 것이므로)
        Meeting m = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found with id: " + meetingId));

        if (m.getEndedAt() == null) {
            m.setEndedAt(LocalDateTime.now());
            // 필요 시 상태 변경: m.setStatus(MeetingStatus.COMPLETED); (이 상태는 Transcribe 완료 후 변경하는 것이 더 적합)
        }
        Meeting savedMeeting = meetingRepository.save(m);

        // ✅ 엔티티를 DTO로 변환하여 반환
        return MeetingDTO.fromEntity(savedMeeting, s3BaseUrl);
    }
}