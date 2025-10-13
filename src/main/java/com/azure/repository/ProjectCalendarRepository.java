package com.azure.repository;

import com.azure.model.calendar.ProjectCalendar;
import org.springframework.data.domain.Page;       // ★ 추가
import org.springframework.data.domain.Pageable; // ★ 추가
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProjectCalendarRepository extends JpaRepository<ProjectCalendar, Long> {

    // ★ DB 레벨 페이징 메서드 사용
    Page<ProjectCalendar> findByProjectId(Long projectId, Pageable pageable); // ★ 유지(주석 제거)

     // 기간 겹침 조회 (FullCalendar 범위 대응)
    @Query("""
      select e from ProjectCalendar e
      where e.project.id = :projectId
        and e.startAt <= :to and e.endAt >= :from
    """)
    List<ProjectCalendar> findByProjectIdAndRangeOverlap(@Param("projectId") Long projectId,
                                                         @Param("from") LocalDateTime from,
                                                         @Param("to") LocalDateTime to);

    // 조직 공용 프로젝트의 앵커 이벤트 찾기
    Optional<ProjectCalendar> findFirstByProject_IdAndTitle(Long projectId, String title);
}
