package com.azure.service.impl;

import com.azure.model.session.SessionStatus;
import com.azure.repository.SessionStatusRepository;
import com.azure.service.SessionStatusService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionStatusServiceImpl implements SessionStatusService {

    private final SessionStatusRepository sessionStatusRepository;
    // 특정 사용자의 세션 상태 조회
    @Override
    public SessionStatus getStatus(Long userId) {
        return sessionStatusRepository.getStatus(userId);
    }
    //  특정 사용자의 세션 상태 변경
    @Override
    public void setStatus(Long userId, SessionStatus status) {
        sessionStatusRepository.setStatus(userId, status);
    }
    // 로그아웃 처리 → OFFLINE 전환
    @Override
    public void logout(Long userId) {
        sessionStatusRepository.setStatus(userId, SessionStatus.OFFLINE);
    }
    // 온라인 상태인 사용자 ID 목록 조회
    @Override
    public List<Long> listOnlineUsers() {
        return sessionStatusRepository.findAllOnlineUserIds(); // Repository에서 Map.keySet() 반환하도록 구현 필요
    }

}
