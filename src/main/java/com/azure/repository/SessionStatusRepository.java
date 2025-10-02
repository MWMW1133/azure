package com.azure.repository;

import com.azure.model.session.SessionStatus;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

@Repository  // ★ 이 한 줄만 추가하면 빈으로 등록됨
public class SessionStatusRepository {

    private static final Map<Long, SessionStatus> statusMap = new ConcurrentHashMap<>();
    // ✅ 사용자 상태 설정 메서드 추가
    public void setStatus(Long userId, SessionStatus status) {
        statusMap.put(userId, status);
    }
    // ✅ 사용자 상태 조회 메서드 추가
    public SessionStatus getStatus(Long userId) {
        return statusMap.getOrDefault(userId, SessionStatus.OFFLINE);
    }
    // ✅ 사용자 상태 제거 메서드 추가
    public void removeStatus(Long userId) {
        statusMap.remove(userId);
    }
    // ✅ 온라인 사용자 ID 목록 반환 메서드 추가
    public List<Long> findAllOnlineUsers() {
        return statusMap.entrySet().stream()
            .filter(e -> e.getValue() == SessionStatus.ONLINE)
            .map(Map.Entry::getKey)
            .toList();
    }

    // ✅ 온라인 사용자 ID 목록 반환 메서드 추가
    public List<Long> findAllOnlineUserIds() {
        return statusMap.entrySet().stream()
                .filter(e -> e.getValue() == SessionStatus.ONLINE)
                .map(Map.Entry::getKey)
                .toList();
    }

}
