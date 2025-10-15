package com.azure.event;

public record ProjectMemberAddedEvent(
        Long projectId,
        String projectTitle,
        String actorName,
        Long addedUserId
) {}
