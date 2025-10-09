package com.azure.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectProposalDTO {
    private Long id;
    private Long proposerId;

    // 작성자 표시용
    private String proposerName;
    private String proposerAvatarUrl;

    private String name;          // 프로젝트명
    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private Long organizationId;
    private Long projectId;

    // JSP 호환용
    public String getTitle() { return name; }
    public LocalDate getEndDate() { return dueDate; }
}
