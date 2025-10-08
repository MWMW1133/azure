package com.azure.service;

import java.util.List;

import com.azure.model.session.SessionStatus;

public interface SessionStatusService {
    /** 특정 사용자의 세션 상태 조회 */
    SessionStatus getStatus(Long userId);

    /** 특정 사용자의 세션 상태 변경 */
    void setStatus(Long userId, SessionStatus status);

    /** 로그아웃 처리 → OFFLINE 전환 */
    void logout(Long userId);
    
    /** 온라인 상태인 사용자 ID 목록 조회 */
    List<Long> listOnlineUsers();

}
