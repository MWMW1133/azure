package com.azure.service;

import com.azure.model.notify.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 사용자 알림 생성/조회/읽음 상태 관리 */
public interface NotificationService {

    /** 사용자에게 알림 생성 */
    Notification notifyUser(Long userId, String type, String payload);

    /** 사용자별 알림 목록(최신순, read가 null이면 전체) */
    Page<Notification> listByUser(Long userId, Pageable pageable, Boolean read);

    /** 안읽은 알림 개수 */
    long unreadCount(Long userId);

    /** 읽음/안읽음 표시(소유자 검증 포함) */
    void markRead(Long notificationId, Long userId, boolean read);
}
