package com.azure.repository;

import com.azure.model.calendar.ProjectCalendar;
import org.springframework.data.domain.Page;       // ★ 추가
import org.springframework.data.domain.Pageable; // ★ 추가
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectCalendarRepository extends JpaRepository<ProjectCalendar, Long> {

    // ★ DB 레벨 페이징 메서드 사용
    Page<ProjectCalendar> findByProjectId(Long projectId, Pageable pageable); // ★ 유지(주석 제거)
}
