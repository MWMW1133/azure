package com.azure.repository;

import com.azure.model.notify.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** 사용자 전체 알림(최신순) */
    Page<Notification> findByUser_IdOrderByIdDesc(Long userId, Pageable pageable);

    /** 읽음 여부로 필터(최신순) — 필드명이 read 이므로 메서드도 Read */
    Page<Notification> findByUser_IdAndReadOrderByIdDesc(Long userId, Boolean read, Pageable pageable);

    /** 소유자 검증용 단건 조회 */
    Optional<Notification> findByIdAndUser_Id(Long id, Long userId);

    /** (선택) 중복 방지용 키가 있으면 활용 */
    boolean existsByTypeAndPayload(String type, String payload);

    /** 안읽은 개수 */
    long countByUser_IdAndReadFalse(Long userId);
}
