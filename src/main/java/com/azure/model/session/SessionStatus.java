package com.azure.model.session;

public enum SessionStatus {
    ONLINE,     // 로그인 후 기본 상태
    AWAY,       // 자리 비움
    BUSY,       // 다른 용무 중
    OFFLINE     // 로그아웃 시
}
