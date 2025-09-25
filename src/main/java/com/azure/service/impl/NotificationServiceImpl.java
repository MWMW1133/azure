package com.azure.service.impl;

import com.azure.model.notify.Notification;
import com.azure.repository.NotificationRepository;
import com.azure.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림 서비스 구현.
 * - 간단한 CRUD 성격으로 시작하여, 향후 푸시/이메일 등 채널 연동 시 확장
 */
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification notifyUser(Long userId, String type, String payload) {
        Notification n = new Notification();
        n.setUser(new com.azure.model.user.User()); n.getUser().setId(userId);
        n.setType(type);
        n.setPayload(payload);
        n.setIsRead(false);
        return notificationRepository.save(n);
    }

    @Override
    public void markRead(Long notificationId, boolean read) {
        Notification n = notificationRepository.findById(notificationId).orElseThrow();
        n.setIsRead(read);
        notificationRepository.save(n);
    }

    @Override @Transactional(readOnly = true)
    public Page<Notification> listByUser(Long userId, Pageable pageable, Boolean isRead) {
        // TODO: 레포에 findByUser_IdAndIsRead(userId, isRead, Pageable) 추가 시 이 로직 교체
        List<Notification> all = notificationRepository.findAll();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<Notification> content = (start > end) ? List.of() : all.subList(start, end);
        return new PageImpl<>(content, pageable, all.size());
    }
}
