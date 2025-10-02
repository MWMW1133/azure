package com.azure.dto;

import com.azure.model.session.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 세션 상태를 표현하는 DTO (DB 저장 X)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    private Long userId;
    private SessionStatus status;
}
