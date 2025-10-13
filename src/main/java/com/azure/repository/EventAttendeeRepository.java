package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.calendar.EventAttendee;

import java.util.List;

public interface EventAttendeeRepository extends JpaRepository<EventAttendee, Long> {

    List<EventAttendee> findByEvent_Id(Long eventId);

    boolean existsByEvent_IdAndUser_Id(Long eventId, Long userId);

    void deleteByEvent_IdAndUser_Id(Long eventId, Long userId);
}
