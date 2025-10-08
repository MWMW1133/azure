package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatChannelDTO {
    private Long id;
    private String channelType;
    private Long projectId;
    private String name;
    private Long createdBy;
    private LocalDateTime createdAt;
}
