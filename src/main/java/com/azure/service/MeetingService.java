package com.azure.service;

import com.azure.model.meeting.Meeting;
import java.time.LocalDateTime;

/**
 * 프로젝트 일정(ProjectCalendar)과 연동되는 회의(실시간 미팅) 수명주기 관리.
 *
 * <h2>기본 개념</h2>
 * - 하나의 프로젝트 일정(event)에서 회의가 시작되고(Recording/Transcript 등과 연계), 종료될 수 있음.
 * - startedAt/endedAt은 지정 없을 시 서버 현재 시각(LocalDateTime.now())로 처리.
 *
 * <h2>확장 아이디어</h2>
 * - "진행 중인 회의는 이벤트당 하나" 정책이 필요하면 start 시 중복 진행 여부 검사.
 * - 권한(참가자/호스트) 검증은 컨트롤러 또는 AOP에서 보강.
 */
public interface MeetingService {

    /**
     * 회의 시작.
     *
     * @param projectEventId 연결할 프로젝트 일정 ID (FK: meetings.event_id)
     * @param projectId      연결할 프로젝트 ID (FK: meetings.project_id)
     * @param startedAt      시작 시각(없으면 now)
     * @return 생성/저장된 Meeting 엔티티
     */
    Meeting startMeeting(Long projectEventId, Long projectId, LocalDateTime startedAt);

    /**
     * 회의 종료.
     *
     * @param meetingId 종료할 회의 ID
     * @param endedAt   종료 시각(없으면 now)
     * @return 갱신된 Meeting 엔티티
     */
    Meeting endMeeting(Long meetingId, LocalDateTime endedAt);
}
