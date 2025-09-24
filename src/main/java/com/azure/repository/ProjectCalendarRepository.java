package com.azure.repository;

import com.azure.model.ProjectCalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectCalendarRepository extends JpaRepository<ProjectCalendarEvent, Long> {}
