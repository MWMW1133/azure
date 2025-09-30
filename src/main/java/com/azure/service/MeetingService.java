package com.azure.service;

import com.azure.model.meeting.Meeting;
import java.time.LocalDateTime;

/** 회의 시작/종료 도메인 서비스 */
public interface MeetingService {

    /**
     * 회의 시작.
     * @param eventId        project_calendars.id (필수)
     * @param organizationId organizations.id (선택)
     * @param projectId      projects.id (선택)
     * @param startedAt      null이면 now
     */
    Meeting startMeeting(Long eventId, Long organizationId, Long projectId, LocalDateTime startedAt);

    /**
     * 회의 종료.
     * @param meetingId meetings.id
     * @param endedAt   null이면 now
     */
    Meeting endMeeting(Long meetingId, LocalDateTime endedAt);

    // [ADD] 조직 공용 회의방 시작(프로젝트/이벤트 모르게)
    Meeting startOrgRoom(Long organizationId, LocalDateTime startedAt);
}
