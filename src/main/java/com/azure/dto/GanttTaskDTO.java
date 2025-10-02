package com.azure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GanttTaskDTO {
    private Long taskId;
    private String title;
    private String assigneeName;
    private String assigneeAvatarUrl;  
    private LocalDate startDate;
    private LocalDate dueDate;
    private BigDecimal progressPct;
    private String workflow;
}
