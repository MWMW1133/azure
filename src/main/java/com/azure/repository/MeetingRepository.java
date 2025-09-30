package com.azure.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.meeting.Meeting;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    // 이벤트 기준 조회
    Page<Meeting> findByEvent_Id(Long eventId, Pageable pageable);

    // 조직 기준 조회(필요 시)
    Page<Meeting> findByOrganization_Id(Long organizationId, Pageable pageable);

    // 프로젝트 기준 조회(필요 시)
    Page<Meeting> findByProject_Id(Long projectId, Pageable pageable);

    // "진행 중" 회의 여부
    boolean existsByEvent_IdAndEndedAtIsNull(Long eventId);
}
