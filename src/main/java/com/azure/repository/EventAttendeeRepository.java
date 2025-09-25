package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.calendar.EventAttendee;
import com.azure.model.calendar.EventAttendeeId;
import java.util.List;

public interface EventAttendeeRepository extends JpaRepository<EventAttendee, EventAttendeeId> {
    List<EventAttendee> findById_ProjectId(Long projectId);
}
