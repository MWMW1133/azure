package com.azure.service.impl;

import com.azure.model.session.SessionStatus;
import com.azure.repository.SessionStatusRepository;
import com.azure.service.SessionStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionStatusServiceImpl implements SessionStatusService {

    private final SessionStatusRepository sessionStatusRepository;

    @Override
    public SessionStatus getStatus(Long userId) {
        return sessionStatusRepository.getStatus(userId);
    }

    @Override
    public void setStatus(Long userId, SessionStatus status) {
        sessionStatusRepository.setStatus(userId, status);
    }

    @Override
    public void logout(Long userId) {
        sessionStatusRepository.setStatus(userId, SessionStatus.OFFLINE);
    }
}
