package com.azure.dto;

import java.time.LocalDateTime;


public class MessageReadDTO {
    private int channelId;
    private int userId;
    private int lastReadMessageId;
    private LocalDateTime updatedAt;


    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(int lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
