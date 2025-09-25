package com.azure.service;

import com.azure.model.meeting.Meeting;
import java.time.LocalDateTime;

/**
 * 프로젝트 일정과 연동되는 회의 수명주기 관리.
 */
public interface MeetingService {
    /** 회의 시작(지정 시각 없으면 현재 시각). */
    Meeting startMeeting(Long projectEventId, Long projectId, LocalDateTime startedAt);

    /** 회의 종료(지정 시각 없으면 현재 시각). */
    Meeting endMeeting(Long meetingId, LocalDateTime endedAt);
}
