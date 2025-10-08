package com.azure.event;

import com.azure.model.meeting.Meeting;
import lombok.Getter;

@Getter
public class MeetingStartedEvent {
    private final Meeting meeting;

    public MeetingStartedEvent(Meeting meeting) {
        this.meeting = meeting;
    }
}
