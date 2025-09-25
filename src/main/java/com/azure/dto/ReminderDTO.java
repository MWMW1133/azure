package com.azure.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReminderDTO {
    private Long id;                    // 알림 ID
    private Long projectEventId;    // 프로젝트 일정 ID
    private Long personalEventId;   // 개인 일정 ID
    private Integer minutesBefore;
    private String method;
    private LocalDateTime createdAt;
    private Long userId;
}
