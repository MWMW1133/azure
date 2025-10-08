// com.azure.event.MeetingEventHandler
package com.azure.event;

import com.azure.dto.MeetingInvite;
import com.azure.model.meeting.Meeting;
import com.azure.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetingEventHandler {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messaging;

    @EventListener
    public void handleMeetingStarted(MeetingStartedEvent event) {
        Meeting meeting = event.getMeeting();

        // (선택) 기존 일반 알림
        notificationService.notifyProjectMembers(
                meeting.getProject().getId(),
                "MEETING_STARTED",
                "프로젝트 [" + meeting.getProject().getName() + "] 회의가 시작되었습니다."
        );

        // ✅ 프로젝트 토픽으로 “초대” 브로드캐스트
        var invite = MeetingInvite.builder()
                .projectId(meeting.getProject().getId())
                .meetingId(meeting.getId())
                .title("프로젝트 [" + meeting.getProject().getName() + "] 회의에 참여하시겠습니까?")
                .sentAt(System.currentTimeMillis())
                .build();

        messaging.convertAndSend(
                "/topic/project/" + meeting.getProject().getId() + "/meeting/invite",
                invite
        );
    }

    @EventListener
    public void handleMeetingEnded(MeetingEndedEvent event) {
        Meeting meeting = event.getMeeting();
        notificationService.notifyProjectMembers(
                meeting.getProject().getId(), "MEETING_ENDED",
                "프로젝트 [" + meeting.getProject().getName() + "] 회의가 종료되었습니다."
        );
    }
}
