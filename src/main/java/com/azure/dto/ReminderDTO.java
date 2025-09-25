package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReminderDTO {
    private Long projectId;
    private Integer minutesBefore;
    private String method;
    private LocalDateTime createdAt;
    private Long userId;
}
