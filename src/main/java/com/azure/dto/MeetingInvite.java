// src/main/java/com/azure/dto/MeetingInvite.java
package com.azure.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MeetingInvite {
    private Long projectId;
    private Long meetingId;
    private Long openedBy;        // 선택
    private String openedByName;  // 선택
    private String title;         // 알림 문구
    private long sentAt;
}
