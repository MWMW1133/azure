package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.meeting.Meeting;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByProjectId(Long projectId);
}
