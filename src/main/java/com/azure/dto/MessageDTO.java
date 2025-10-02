package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private Long channelId;
    private Long authorId;
    private String body;
    private Long fileId;
    private Long replyToId;
    private LocalDateTime createdAt;
}
