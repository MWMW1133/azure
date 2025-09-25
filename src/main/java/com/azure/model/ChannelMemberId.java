package com.azure.model;


import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Data;
@Data @Embeddable
public class ChannelMemberId implements Serializable {
    private Long channelId;
    private Long userId;
}
