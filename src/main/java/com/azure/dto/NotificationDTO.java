package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String type;
    private String payload;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
