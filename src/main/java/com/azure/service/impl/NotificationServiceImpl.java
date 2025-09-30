package com.azure.service.impl;

import com.azure.model.notify.Notification;
import com.azure.model.user.User;
import com.azure.repository.NotificationRepository;
import com.azure.service.NotificationService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 서비스 구현.
 * - DB 페이징으로 목록 조회
 * - 소유자 검증 후 읽음 상태 변경
 */
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification notifyUser(Long userId, String type, String payload) {
        Notification n = new Notification();
        n.setUser(new User()); n.getUser().setId(userId);
        n.setType(type);
        n.setPayload(payload);
        n.setRead(false);                     // ← 엔티티 필드명이 read
        return notificationRepository.save(n);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> listByUser(Long userId, Pageable pageable, Boolean read) {
        if (read == null) {
            return notificationRepository.findByUser_IdOrderByIdDesc(userId, pageable);
        }
        return notificationRepository.findByUser_IdAndReadOrderByIdDesc(userId, read, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUser_IdAndReadFalse(userId);
    }

    @Override
    public void markRead(Long notificationId, Long userId, boolean read) {
        Notification n = notificationRepository.findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new NotFoundException("notification not found: " + notificationId));
        n.setRead(read);                       // ← 여기서도 setRead
        notificationRepository.save(n);
    }
}
