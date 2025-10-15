package com.azure.listener;

import com.azure.event.ProposalStatusChangedEvent;
import com.azure.model.notify.NotificationType;
import com.azure.model.project.ProjectProposal;
import com.azure.repository.ProjectProposalRepository;
import com.azure.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProposalNotificationListener {

    private final ProjectProposalRepository proposalRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalStatusChanged(ProposalStatusChangedEvent event) {
        ProjectProposal p = proposalRepository.findById(event.proposalId()).orElse(null);
        if (p == null) return;

        Long proposerId     = p.getProposer().getId();
        Long organizationId = p.getOrganization().getId();
        Long projectId      = (p.getProject() != null ? p.getProject().getId() : null);
        String status       = p.getStatus().name();
        String name         = p.getName();

        String message = ("APPROVED".equals(status))
                ? "‘" + name + "’ 제안이 승인되었습니다."
                : "‘" + name + "’ 제안이 거절되었습니다.";

        Map<String, Object> payload = new HashMap<>();
        payload.put("proposalId", p.getId());
        payload.put("projectId", projectId);
        payload.put("organizationId", organizationId);
        payload.put("status", status);
        payload.put("name", name);
        payload.put("message", message);

        try {
            String json = objectMapper.writeValueAsString(payload);
            notificationService.notifyUser(
                proposerId,
                NotificationType.PROPOSAL_STATUS_CHANGED.name(),
                json
            );
        } catch (Exception e) {
            notificationService.notifyUser(
                proposerId,
                NotificationType.PROPOSAL_STATUS_CHANGED.name(),
                message
            );
        }
    }
}
