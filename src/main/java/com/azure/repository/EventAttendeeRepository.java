package com.azure.repository;

import com.azure.model.EventAttendee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAttendeeRepository extends JpaRepository<EventAttendee, com.azure.model.EventAttendeeId> {}
