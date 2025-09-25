package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.notify.Notification;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);
}
