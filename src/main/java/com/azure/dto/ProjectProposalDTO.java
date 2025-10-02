package com.azure.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectProposalDTO {
    private Long id;
    private Long proposerId;
    private String name;
    private String description;
    private String status;
    private LocalDate startDate;   // 예상 시작일
    private LocalDate dueDate;     // 예상 마감일
    private LocalDateTime createdAt;
    private Long organizationId;
    private Long projectId;
}