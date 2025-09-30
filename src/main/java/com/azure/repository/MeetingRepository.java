package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.meeting.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    // ★ 프로젝트별 회의 목록(페이징)
    Page<Meeting> findByProject_Id(Long projectId, Pageable pageable);

    // [옵션] "진행 중(미종료)" 회의 여부 검사/조회
    boolean existsByEvent_IdAndEndedAtIsNull(Long eventId);
    Page<Meeting> findByEvent_IdAndEndedAtIsNull(Long eventId, Pageable pageable);
}
