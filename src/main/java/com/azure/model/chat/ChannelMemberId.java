package com.azure.model.chat;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class ChannelMemberId implements Serializable {
    private Long channelId;
    private Long userId;
}
