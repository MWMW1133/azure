package com.azure.dto;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    private Long projectId;
    private Long parentTaskId;
    private String title;
    private Long assigneeId;
    private Long reporterId;
    private Long workflowsId;
    private Integer priorityId;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime completedAt;
    private Double progressPct;
    private Double kanbanRank;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
