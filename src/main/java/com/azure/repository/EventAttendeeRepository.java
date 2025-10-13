package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.azure.model.calendar.EventAttendee;

import java.util.List;

public interface EventAttendeeRepository extends JpaRepository<EventAttendee, Long> {

    List<EventAttendee> findByEvent_Id(Long eventId);

    boolean existsByEvent_IdAndUser_Id(Long eventId, Long userId);

    void deleteByEvent_IdAndUser_Id(Long eventId, Long userId);

    @Modifying
    @Query("delete from EventAttendee ea where ea.event.id = :eventId")
    void deleteByEvent_Id(Long eventId);   // ★ 추가

}
