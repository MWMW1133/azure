package com.azure.service.impl;

import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.meeting.Meeting;
import com.azure.repository.MeetingRepository;
import com.azure.repository.ProjectCalendarRepository;
import com.azure.service.MeetingService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 회의 서비스 구현.
 *
 * <h2>역할</h2>
 * - 프로젝트 일정(ProjectCalendar)에 연결된 회의(Meeting)의 시작/종료를 처리.
 * - 외부(컨트롤러)에서 시간/권한/정책 등을 위임받아 비즈니스 규칙을 캡슐화.
 *
 * <h2>트랜잭션</h2>
 * - 클래스 레벨 @Transactional: 쓰기 메서드 기본 트랜잭션.
 * - 읽기 전용 조회가 필요할 경우 메서드에 @Transactional(readOnly=true) 부여.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final ProjectCalendarRepository projectCalendarRepository;

    @Override
    public Meeting startMeeting(Long projectEventId, Long projectId, LocalDateTime startedAt) {
        // 1) 이벤트 존재 확인 (미존재 시 404)
        ProjectCalendar event = projectCalendarRepository.findById(projectEventId)
                .orElseThrow(() -> new NotFoundException("Project event not found: " + projectEventId));

        // [옵션] 이벤트가 특정 프로젝트에 속하는지 검증하고 싶다면 아래와 같이 체크
        // if (event.getProject() != null && projectId != null
        //     && !event.getProject().getId().equals(projectId)) {
        //     throw new BadRequestException("이벤트와 프로젝트가 일치하지 않습니다.");
        // }

        // [옵션] "이벤트당 진행 중인 회의는 하나" 정책을 원하면 여기서 중복 진행 여부 검사
        // (예: meetingRepository.existsByEvent_IdAndEndedAtIsNull(projectEventId))

        // 2) 엔티티 조립 (연관 FK는 ID만 세팅한 얕은 엔티티로 연결해도 충분)
        Meeting m = new Meeting();
        m.setEvent(event);
        m.setProject(new com.azure.model.project.Project()); // FK만 필요하므로 ID만 설정
        m.getProject().setId(projectId);

        // 3) 시간 기본값 처리: null이면 현재 시각
        m.setStartedAt(startedAt != null ? startedAt : LocalDateTime.now());

        // created_at은 DB가 채운다면 엔티티에 insertable=false/updatable=false 권장(엔티티 주석 참고)
        return meetingRepository.save(m);
    }

    @Override
    public Meeting endMeeting(Long meetingId, LocalDateTime endedAt) {
        // 1) 회의 존재 확인
        Meeting m = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("Meeting not found: " + meetingId));

        // [주의] 비즈니스 규칙: 종료 시각은 시작 시각 이후여야 함. 필요 시 아래처럼 검증
        // LocalDateTime end = (endedAt != null ? endedAt : LocalDateTime.now());
        // if (m.getStartedAt() != null && !end.isAfter(m.getStartedAt())) {
        //     throw new BadRequestException("endedAt은 startedAt 이후여야 합니다.");
        // }
        // m.setEndedAt(end);

        // 현재 구현: 단순 종료(지정 없으면 now)
        m.setEndedAt(endedAt != null ? endedAt : LocalDateTime.now());

        // [옵션] 이미 종료된 회의(endedAt != null)에 대한 재종료 방지/경고 로직 추가 가능
        return meetingRepository.save(m);
    }
}
