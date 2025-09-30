package com.azure.event;

import com.azure.model.meeting.Meeting;
import lombok.Getter;

@Getter
public class MeetingEndedEvent {
    private final Meeting meeting;

    public MeetingEndedEvent(Meeting meeting) {
        this.meeting = meeting;
    }
}
