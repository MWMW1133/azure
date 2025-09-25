package com.azure.repository;

import com.azure.model.EventAttendeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAttendeeRepository extends JpaRepository<EventAttendeeEntity, Long> {
}
