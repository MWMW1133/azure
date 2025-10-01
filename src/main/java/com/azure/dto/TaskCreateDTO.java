package com.azure.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class TaskCreateDTO {
    private Long projectId;
    private Long parentTaskId;
    private String title;

    private Long assigneeId;     // 담당자 (nullable 가능)
    private Long workflowsId;    // 초기 상태 (Assignments 등)
    private Integer priorityId;  // 우선순위

    private LocalDate startDate;
    private LocalDate dueDate;
}
