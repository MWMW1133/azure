package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.calendar.PersonalCalendar;

public interface PersonalCalendarRepository extends JpaRepository<PersonalCalendar, Long> { }
