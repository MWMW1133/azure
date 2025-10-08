package com.azure.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChannelMemberDTO {
    private Long channelId;
    private Long userId;
    private LocalDateTime joinedAt;
}
