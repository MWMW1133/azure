package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String loginId;
    private String passwordHash;
    private String name;
    private String avatarUrl;
    private String workStatus;
    private LocalDateTime createdAt;
    private Long organizationId;
}
