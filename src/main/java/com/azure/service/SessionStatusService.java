package com.azure.service;

import com.azure.model.session.SessionStatus;

public interface SessionStatusService {
    /** 특정 사용자의 세션 상태 조회 */
    SessionStatus getStatus(Long userId);

    /** 특정 사용자의 세션 상태 변경 */
    void setStatus(Long userId, SessionStatus status);

    /** 로그아웃 처리 → OFFLINE 전환 */
    void logout(Long userId);
}
