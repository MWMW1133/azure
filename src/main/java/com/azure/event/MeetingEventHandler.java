package com.azure.event;

import com.azure.model.meeting.Meeting;
import com.azure.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetingEventHandler {

    private final NotificationService notificationService;

    @EventListener
    public void handleMeetingStarted(MeetingStartedEvent event) {
        Meeting meeting = event.getMeeting();
        notificationService.notifyProjectMembers(
                meeting.getProject().getId(),
                "MEETING_STARTED",
                "프로젝트 [" + meeting.getProject().getName() + "] 회의가 시작되었습니다."
        );
    }

    @EventListener
    public void handleMeetingEnded(MeetingEndedEvent event) {
        Meeting meeting = event.getMeeting();
        notificationService.notifyProjectMembers(
                meeting.getProject().getId(),
                "MEETING_ENDED",
                "프로젝트 [" + meeting.getProject().getName() + "] 회의가 종료되었습니다."
        );
    }
}
