package com.azure.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class JoinGrant {
    private String joinUrl;   // 예: /meetings/join?token=...
    private long expiresAt;   // epoch millis
}
