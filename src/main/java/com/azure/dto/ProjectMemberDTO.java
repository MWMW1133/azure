package com.azure.dto;
import lombok.Data;

@Data
public class ProjectMemberDTO {
    private Long projectId;
    private Long userId;
    private String role;
}
