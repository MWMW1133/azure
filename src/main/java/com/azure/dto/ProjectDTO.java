package com.azure.dto;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private Long proposalId;
    private Long organizationId;
}
