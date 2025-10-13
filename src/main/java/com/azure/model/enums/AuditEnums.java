package com.azure.model.enums;

public final class AuditEnums {
  private AuditEnums() {}

  public enum EntityType { TASK, FILE }
  public enum ActionType {
    ASSIGNEE_CHANGED,     // 담당자 변경
    WORKFLOW_CHANGED,     // 상태(워크플로우) 변경
    PRIORITY_CHANGED,     // 우선순위 변경
    FILE_ATTACHED,        // 파일 업로드
    FILE_REMOVED         // 파일 삭제
  }
}