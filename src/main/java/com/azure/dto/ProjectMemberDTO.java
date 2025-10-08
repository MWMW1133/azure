package com.azure.dto;

import lombok.Data;

@Data
public class ProjectMemberDTO {
    private Long projectId;
    private Long userId;
    private String userName;       // User 테이블 join 결과
    private String userAvatarUrl;  // User.avatar_url join 결과
    private UserRole role;         // 프로젝트 내 역할
}
