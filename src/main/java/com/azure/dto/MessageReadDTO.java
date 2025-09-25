package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageReadDTO {
    private Long channelId;
    private Long userId;
    private Long lastReadMessageId;
    private LocalDateTime updatedAt;
}
