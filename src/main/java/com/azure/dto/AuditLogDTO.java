package com.azure.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogDTO {
    private Long id;
    private Long actorId;
    private String entityType;
    private Long entityId;
    private String action;
    private String diffJson;
    private LocalDateTime createdAt;
}
