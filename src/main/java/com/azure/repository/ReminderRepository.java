package com.azure.repository;

import com.azure.model.reminder.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 리마인더가 '지금~1분 뒤' 사이에 도래하는 건만 뽑아온다.
 * MySQL: e.start_at - INTERVAL r.minutes_before MINUTE BETWEEN NOW() AND NOW()+1분
 */
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    // 프로젝트 일정 중에서 리마인더가 도래하는 것 조회
    @Query(value = """
        SELECT r.* FROM reminders r
        JOIN project_calendars e ON r.project_event_id = e.id
        WHERE (e.start_at - INTERVAL r.minutes_before MINUTE)
              BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL :windowSec SECOND)
        """, nativeQuery = true)
    List<Reminder> findDueProjectReminders(@Param("windowSec") int windowSec);
    // 개인 일정 중에서 리마인더가 도래하는 것 조회
    @Query(value = """
        SELECT r.* FROM reminders r
        JOIN personal_calendars e ON r.personal_event_id = e.id
        WHERE (e.start_at - INTERVAL r.minutes_before MINUTE)
              BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL :windowSec SECOND)
        """, nativeQuery = true)
    List<Reminder> findDuePersonalReminders(@Param("windowSec") int windowSec);
    // 특정 유저의 모든 리마인더 조회
    List<Reminder> findByUser_Id(Long userId); 
    // 특정 유저의 특정 알림 방식 리마인더 조회
    List<Reminder> findByUser_IdAndMethod(Long userId, String method);
}
