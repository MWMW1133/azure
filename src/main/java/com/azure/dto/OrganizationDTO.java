package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrganizationDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private Long userId;
    private String role;
}
