package com.azure.event;

public record ChatMessageCreatedEvent(
        Long channelId,
        Long messageId,
        Long authorId,
        String preview
) {}
