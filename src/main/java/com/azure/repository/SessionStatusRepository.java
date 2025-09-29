package com.azure.repository;

import com.azure.model.session.SessionStatus;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class SessionStatusRepository {

    private static final Map<Long, SessionStatus> statusMap = new ConcurrentHashMap<>();

    public void setStatus(Long userId, SessionStatus status) {
        statusMap.put(userId, status);
    }

    public SessionStatus getStatus(Long userId) {
        return statusMap.getOrDefault(userId, SessionStatus.OFFLINE);
    }

    public void removeStatus(Long userId) {
        statusMap.remove(userId);
    }
}
