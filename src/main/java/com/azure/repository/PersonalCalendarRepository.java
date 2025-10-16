package com.azure.repository;

import com.azure.model.calendar.PersonalCalendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PersonalCalendar 전용 리포지토리
 *
 * 기본 기능:
 * - JpaRepository가 제공하는 CRUD + findAll(Pageable) 사용 가능
 *
 * 확장 기능(권장):
 * - 사용자 기준 페이징, 완료 여부 필터, 정렬, 기간 필터
 */
public interface PersonalCalendarRepository extends JpaRepository<PersonalCalendar, Long> {

    /** 특정 사용자의 개인 일정만 페이징으로 조회 */
    Page<PersonalCalendar> findByCreatedBy_Id(Long userId, Pageable pageable);

    /** 특정 사용자 + 완료/미완료 필터 */
    Page<PersonalCalendar> findByCreatedBy_IdAndIsDone(Long userId, Boolean isDone, Pageable pageable);

    /** 특정 사용자 + 시작일 내림차순(최신 먼저) */
    Page<PersonalCalendar> findByCreatedBy_IdOrderByStartAtDesc(Long userId, Pageable pageable);

    /** 특정 사용자 + 기간 필터(시작~종료 사이) */
    Page<PersonalCalendar> findByCreatedBy_IdAndStartAtBetween(
            Long userId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    /** 완료/미완료 개수 집계(대시보드 등) */
    long countByCreatedBy_IdAndIsDone(Long userId, Boolean isDone);

    // 오늘 일정 조회
    List<PersonalCalendar> findByCreatedBy_IdAndStartAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    /** 특정 유저의 일정 중, [start,end] 구간과 겹치는 개인일정 모두 반환 */
    @Query("""
        select e
          from PersonalCalendar e
         where e.createdBy.id = :userId
           and e.startAt < :end
           and e.endAt   > :start
        """)
    List<PersonalCalendar> findOverlappingForUser(@Param("userId") Long userId,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);
}
