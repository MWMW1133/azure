package com.azure.event;

public record TaskWorkflowChangedEvent(
        Long projectId,
        Long taskId,
        String fromStage,
        String toStage,
        Long actorUserId,
        String taskTitle
) {}
