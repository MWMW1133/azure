package com.azure.service;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * "지금 이 순간 접속 중" 프레즌스 메모리 저장소.
 * - meetingId -> (sessionId -> displayName)
 * - 서버 재시작 시 초기화(요건상 OK). 영속 필요하면 Redis 등으로 교체.
 */
@Service
public class PresenceService {
    private final Map<Long, Map<String, String>> online = new ConcurrentHashMap<>();

    public void join(Long meetingId, String sessionId, String name) {
        online.computeIfAbsent(meetingId, k -> new ConcurrentHashMap<>())
                .put(sessionId, name);
    }

    public void leave(String sessionId) {
        online.values().forEach(map -> map.remove(sessionId));
    }

    public Collection<String> list(Long meetingId) {
        return online.getOrDefault(meetingId, Map.of()).values();
    }
}
