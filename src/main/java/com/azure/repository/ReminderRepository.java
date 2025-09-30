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

    @Query(value = """
        SELECT r.* FROM reminders r
        JOIN project_calendars e ON r.project_event_id = e.id
        WHERE (e.start_at - INTERVAL r.minutes_before MINUTE)
              BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL :windowSec SECOND)
        """, nativeQuery = true)
    List<Reminder> findDueProjectReminders(@Param("windowSec") int windowSec);

    @Query(value = """
        SELECT r.* FROM reminders r
        JOIN personal_calendars e ON r.personal_event_id = e.id
        WHERE (e.start_at - INTERVAL r.minutes_before MINUTE)
              BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL :windowSec SECOND)
        """, nativeQuery = true)
    List<Reminder> findDuePersonalReminders(@Param("windowSec") int windowSec);
}
