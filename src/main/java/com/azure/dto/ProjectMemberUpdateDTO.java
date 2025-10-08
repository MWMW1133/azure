package com.azure.dto;

import lombok.Data;

@Data
public class ProjectMemberUpdateDTO {
    private Long projectId;
    private Long userId;
    private UserRole role;
}
