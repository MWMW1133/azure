package com.azure.event;

public record ProposalStatusChangedEvent(
        Long proposalId,
        String newStatus
) {}
