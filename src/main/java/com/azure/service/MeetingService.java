package com.azure.service;

import com.azure.model.meeting.Meeting;
import java.time.LocalDateTime;

/** 회의 시작/종료 도메인 서비스 */
public interface MeetingService {

    /**
     * 회의 시작.
     * @param eventId        project_calendars.id (FK)
     * @param organizationId organizations.id (옵션)
     * @param startedAt      null이면 now
     */
    Meeting startMeeting(Long eventId, Long organizationId, LocalDateTime startedAt);

    /**
     * 회의 종료.
     * @param meetingId meetings.id
     * @param endedAt   null이면 now
     */
    Meeting endMeeting(Long meetingId, LocalDateTime endedAt);
}
