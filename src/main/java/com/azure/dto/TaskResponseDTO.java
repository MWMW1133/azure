package com.azure.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TaskResponseDTO {
    private Long id;
    private Long projectId;
    private Long parentTaskId;
    private String title;

    private Long assigneeId;
    private String assigneeName;
    private String assigneeAvatarUrl;

    private Long workflowsId;
    private String workflowName;

    private Integer priorityId;
    private String priorityName;
    private Integer priorityLevel;

    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime completedAt;

    private Double progressPct;
    private Double kanbanRank;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


