package com.azure.dto;
import lombok.Data;

@Data
public class TaskDependencyDTO {
    private Long id;
    private Long predecessorId;
    private Long successorId;
    private String type;
    private Integer lagDays;
}
