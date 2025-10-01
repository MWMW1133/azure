package com.azure.dto;

import lombok.Data;

@Data
public class OrganizationCreateDTO {
    private String name;      // 회사명
    private Long userId;      // 소속 사용자
    private UserRole role;    // 역할 (LEADER / MEMBER)
}
