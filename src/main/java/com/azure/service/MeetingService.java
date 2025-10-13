package com.azure.service;

import com.azure.model.meeting.Meeting;

public interface MeetingService {
    Meeting startMeeting(Long organizationId, Long projectId);
    Meeting endMeeting(Long meetingId);
}
