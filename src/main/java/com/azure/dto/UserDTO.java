package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String passwordHash;
    private String name;
    private String avatarUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long organizationId;
}
