package com.azure.controller;

import com.azure.dto.UserSession;
import com.azure.model.session.SessionStatus;
import com.azure.service.SessionStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/session-status")
@RequiredArgsConstructor
public class SessionStatusController {

    private final SessionStatusService sessionStatusService;

    /** 상태 조회 */
    @GetMapping("/{userId}")
    public UserSession getStatus(@PathVariable Long userId) {
        return new UserSession(userId, sessionStatusService.getStatus(userId));
    }

    /** 상태 변경 */
    @PostMapping("/{userId}")
    public UserSession setStatus(@PathVariable Long userId,
                                 @RequestParam SessionStatus status) {
        sessionStatusService.setStatus(userId, status);
        return new UserSession(userId, status);
    }

    /** 로그아웃 처리 */
    @PostMapping("/{userId}/logout")
    public UserSession logout(@PathVariable Long userId) {
        sessionStatusService.logout(userId);
        return new UserSession(userId, SessionStatus.OFFLINE);
    }
}
