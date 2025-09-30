package com.azure.service.impl;

import com.azure.model.Organization;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.meeting.Meeting;
import com.azure.repository.MeetingRepository;
import com.azure.repository.ProjectCalendarRepository;
import com.azure.service.MeetingService;
import com.azure.service.exception.BadRequestException;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 회의 서비스 구현.
 *
 * 역할
 * - 프로젝트 일정(ProjectCalendar)에 연결된 회의(Meeting)의 시작/종료 처리.
 * - 비즈니스 규칙(입력 검증, 중복 회의 방지, 시간 검증) 캡슐화.
 *
 * 트랜잭션
 * - 클래스 레벨 @Transactional: 쓰기 메서드 기본 트랜잭션.
 * - 읽기 전용 조회는 @Transactional(readOnly = true) 권장.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final ProjectCalendarRepository projectCalendarRepository;

    /**
     * 회의 시작.
     * @param eventId        project_calendars.id (필수)
     * @param organizationId organizations.id (선택)
     * @param startedAt      지정 없으면 now
     */
    @Override
    public Meeting startMeeting(Long eventId, Long organizationId, LocalDateTime startedAt) {
        if (eventId == null) throw new BadRequestException("eventId는 필수입니다.");

        // 1) 이벤트 존재 확인
        ProjectCalendar event = projectCalendarRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Project event not found: " + eventId));

        // 2) 중복 진행 회의 방지(정책에 따라 제거 가능)
        if (meetingRepository.existsByEvent_IdAndEndedAtIsNull(eventId)) {
            throw new BadRequestException("이미 진행 중인 회의가 있습니다.");
        }

        // 3) 엔티티 조립 (연관 FK는 ID만 세팅한 얕은 엔티티로 연결)
        Meeting m = new Meeting();
        m.setEvent(event);

        if (organizationId != null) {
            Organization org = new Organization();
            org.setId(organizationId);
            m.setOrganization(org);
        }

        // 4) 시간 기본값 처리
        m.setStartedAt(startedAt != null ? startedAt : LocalDateTime.now());

        return meetingRepository.save(m);
    }

    /**
     * 회의 종료.
     * @param meetingId meetings.id
     * @param endedAt   지정 없으면 now
     */
    @Override
    public Meeting endMeeting(Long meetingId, LocalDateTime endedAt) {
        // 1) 회의 존재 확인
        Meeting m = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("Meeting not found: " + meetingId));

        // 2) 종료 시각 검증: 시작 이후여야 함
        LocalDateTime end = (endedAt != null ? endedAt : LocalDateTime.now());
        if (m.getStartedAt() != null && !end.isAfter(m.getStartedAt())) {
            throw new BadRequestException("endedAt은 startedAt 이후여야 합니다.");
        }

        // 3) 종료 처리
        m.setEndedAt(end);
        return meetingRepository.save(m);
    }
}
