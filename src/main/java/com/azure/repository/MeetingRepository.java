package com.azure.repository;

import com.azure.model.enums.MeetingStatus;
import com.azure.model.meeting.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Page<Meeting> findByOrganization_Id(Long organizationId, Pageable pageable);
    Page<Meeting> findByProject_Id(Long projectId, Pageable pageable);
    Page<Meeting> findByOrganization_IdAndProject_Id(Long organizationId, Long projectId, Pageable pageable);
    // 폴러용: 상태가 TRANSCRIBING이고 녹음 파일이 존재하는 회의만
    List<Meeting> findByStatus(MeetingStatus status);
    // 👇 JOIN FETCH를 확장하여 User의 Organization 정보까지 모두 즉시 로딩하도록 수정
    @Query("SELECT m FROM Meeting m " +
            "LEFT JOIN FETCH m.organization " +
            "JOIN FETCH m.project p " +
            "LEFT JOIN FETCH p.owner o LEFT JOIN FETCH o.organization " +
            "JOIN FETCH m.event e " +
            "JOIN FETCH e.createdBy cb LEFT JOIN FETCH cb.organization " +
            "WHERE m.id = :id")
    Optional<Meeting> findByIdWithEvent(@Param("id") Long id);
}

