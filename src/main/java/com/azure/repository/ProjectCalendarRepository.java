package com.azure.repository;

import com.azure.model.calendar.ProjectCalendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProjectCalendarRepository extends JpaRepository<ProjectCalendar, Long> {

    Page<ProjectCalendar> findByProjectId(Long projectId, Pageable pageable);

    @Query("""
      select e from ProjectCalendar e
      where e.project.id = :projectId
        and e.startAt <= :to and e.endAt >= :from
    """)
    List<ProjectCalendar> findByProjectIdAndRangeOverlap(@Param("projectId") Long projectId,
                                                         @Param("from") LocalDateTime from,
                                                         @Param("to") LocalDateTime to);

    // 표준 명명 규칙에 맞게 수정 (기능은 동일)
    Optional<ProjectCalendar> findByProjectIdAndTitle(Long projectId, String title);
}