package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkflowDTO {
    private Long id;
    private Long projectId;
    private String name;
    private Integer sortOrder;
    private Boolean isBlocking;
    private Boolean isTerminal;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}
