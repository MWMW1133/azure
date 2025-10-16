package com.azure.repository;

import com.azure.model.meeting.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Page<Meeting> findByOrganization_Id(Long organizationId, Pageable pageable);
    Page<Meeting> findByProject_Id(Long projectId, Pageable pageable);
    Page<Meeting> findByOrganization_IdAndProject_Id(Long organizationId, Long projectId, Pageable pageable);
}
