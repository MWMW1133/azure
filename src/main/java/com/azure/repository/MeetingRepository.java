package com.azure.repository;

import com.azure.model.meeting.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Page<Meeting> findByOrganization_Id(Long organizationId, Pageable pageable);
    Page<Meeting> findByProject_Id(Long projectId, Pageable pageable);
    Page<Meeting> findByOrganization_IdAndProject_Id(Long organizationId, Long projectId, Pageable pageable);

    // 👇 JOIN FETCH m.project 를 추가하여 project 정보도 함께 가져오도록 수정
    @Query("SELECT m FROM Meeting m JOIN FETCH m.event JOIN FETCH m.project WHERE m.id = :id")
    Optional<Meeting> findByIdWithEvent(@Param("id") Long id);
}

