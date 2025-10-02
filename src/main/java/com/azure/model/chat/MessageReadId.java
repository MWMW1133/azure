package com.azure.model.chat;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class MessageReadId implements Serializable {
    private Long channelId;
    private Long userId;
}
