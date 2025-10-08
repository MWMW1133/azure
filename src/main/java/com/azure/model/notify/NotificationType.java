package com.azure.model.notify;

public enum NotificationType {
    CHAT_MESSAGE,                 // 채팅 도착
    TASK_WORKFLOW_CHANGED,        // 태스크 단계 변경
    PROJECT_MEMBER_ADDED,         // 프로젝트 멤버로 추가됨
    PROPOSAL_STATUS_CHANGED,      // 내 제안의 상태가 바뀜(승인/거절)
    NEW_PROPOSAL_CREATED,          // 관리자에게: 새 제안 생성 알림
    PROJECT_MEMBER_REMOVED,       // 프로젝트 멤버에서 제거됨
    // 회사 초대가 없음 만들어야됨
    INVITE_ORGANIZATION    // 조직에 사용자를 초대
}
