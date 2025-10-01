package com.azure.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrganizationDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private Long userId;     // 소속 사용자 ID
    private UserRole role;   // 회사 내 역할 (LEADER, MEMBER)
}
