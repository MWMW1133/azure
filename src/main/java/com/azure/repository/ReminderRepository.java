package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.reminder.Reminder;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByProjectEventId(Long projectEventId);
    List<Reminder> findByPersonalEventId(Long personalEventId);
    List<Reminder> findByUserId(Long userId);
}
