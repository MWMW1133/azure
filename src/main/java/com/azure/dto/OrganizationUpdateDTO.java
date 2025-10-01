package com.azure.dto;

import lombok.Data;

@Data
public class OrganizationUpdateDTO {
    private Long id;          // 회사 ID
    private Long userId;      // 수정할 사용자 ID
    private UserRole role;    // 변경할 역할
    private String name;      // 회사명 수정 (옵션)
}
