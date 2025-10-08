package com.azure.event;

public record ProjectMemberRemovedEvent(
        Long projectId,
        Long removedUserId,
        Long removedByUserId
) {}
