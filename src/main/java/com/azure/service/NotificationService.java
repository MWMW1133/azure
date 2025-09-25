package com.azure.service;

import com.azure.model.notify.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 사용자 알림 생성/읽음 상태 관리.
 */
public interface NotificationService {
    /** 사용자에게 알림 생성. */
    Notification notifyUser(Long userId, String type, String payload);

    /** 읽음/읽지 않음 표시. */
    void markRead(Long notificationId, boolean read);

    /** 사용자별 알림 목록(필터/페이징). */
    Page<Notification> listByUser(Long userId, Pageable pageable, Boolean isRead);
}
