package com.azure.service;

import com.azure.dto.MeetingDTO;
import com.azure.model.meeting.Meeting;

public interface MeetingService {
    MeetingDTO startMeeting(Long organizationId, Long projectId); // ✅ 반환 타입 변경
    MeetingDTO endMeeting(Long meetingId);
}
