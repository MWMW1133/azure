package com.azure.event;

import com.azure.model.notify.NotificationType;
import com.azure.service.NotificationService;
import com.azure.util.Jsons;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationService notificationService;

    // 1) 채팅 메시지 도착
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChatMessageCreatedEvent e) {
        String payload = Jsons.stringify(Map.of(
                "channelId", e.channelId(),
                "messageId", e.messageId(),
                "preview", e.preview()
        ));
        // author 제외는 service 내부에서 처리
        notificationService.notifyUser(e.authorId(), NotificationType.CHAT_MESSAGE.name(), payload);
    }

    // 2) 태스크 워크플로 변경
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TaskWorkflowChangedEvent e) {
        String payload = Jsons.stringify(Map.of(
                "projectId", e.projectId(),
                "taskId", e.taskId(),
                "taskTitle", e.taskTitle(),
                "from", e.fromStage(),
                "to", e.toStage(),
                "actorUserId", e.actorUserId()
        ));
        notificationService.notifyUser(e.actorUserId(), NotificationType.TASK_WORKFLOW_CHANGED.name(), payload);
    }

    // 3-1) 프로젝트 멤버 추가
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProjectMemberAddedEvent e) {
        String payload = Jsons.stringify(Map.of(
                "projectId", e.projectId(),
                "addedByUserId", e.addedByUserId()
        ));
        notificationService.notifyUser(e.addedUserId(), NotificationType.PROJECT_MEMBER_ADDED.name(), payload);
    }
    // 3-2) 프로젝트 멤버 제거
    @TransactionalEventListener
    public void on(ProjectMemberRemovedEvent e) {
    String payload = Jsons.stringify(Map.of(
        "projectId", e.projectId(),
        "removedByUserId", e.removedByUserId()
    ));
    notificationService.notifyUser(e.removedUserId(), NotificationType.PROJECT_MEMBER_REMOVED.name(), payload);
}

    // 4) 프로젝트 제안 상태 변경
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProposalStatusChangedEvent e) {
        String payload = Jsons.stringify(Map.of(
                "proposalId", e.proposalId(),
                "status", e.newStatus()
        ));
        // proposerId 조회는 service 안에서 처리 가능
        notificationService.notifyUser(null, NotificationType.PROPOSAL_STATUS_CHANGED.name(), payload);
    }

    // 5) 신규 제안 생성 (관리자 알림)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProposalCreatedEvent e) {
        String payload = Jsons.stringify(Map.of(
                "proposalId", e.proposalId()
        ));
        notificationService.notifyUser(null, NotificationType.NEW_PROPOSAL_CREATED.name(), payload);
    }
    
}
