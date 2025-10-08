package com.azure.event;

public record ProjectMemberAddedEvent(
        Long projectId,
        Long addedUserId,
        Long addedByUserId
) {}
