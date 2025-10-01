package com.azure.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TaskUpdateDTO {
    private Long id;             // 어떤 Task를 수정할지 지정
    private String title;

    private Long assigneeId;
    private Long workflowsId;
    private Integer priorityId;

    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime completedAt; // 완료 처리 시점

    private Double progressPct;  // 진행률 (0~100)
    private Double kanbanRank;   // 칸반 정렬값
}
