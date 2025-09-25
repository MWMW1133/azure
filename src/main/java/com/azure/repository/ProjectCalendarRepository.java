package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.calendar.ProjectCalendar;
import java.util.List;

public interface ProjectCalendarRepository extends JpaRepository<ProjectCalendar, Long> {
    List<ProjectCalendar> findByProjectId(Long projectId);
}
