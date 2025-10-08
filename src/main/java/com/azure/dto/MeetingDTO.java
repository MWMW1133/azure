// src/main/java/com/azure/dto/MeetingDTO.java
package com.azure.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 주의: meetingName/meetingDescription은 실제 Meeting 엔티티에 없으므로
 * 보통 ProjectCalendar(event)의 title/description을 매핑해서 채움.
 */
@Data
public class MeetingDTO {
    private Long id;
    private Long eventId;

    private String meetingName;         // <- event.title 등에서 채우기
    private String meetingDescription;  // <- event.description 등에서 채우기

    private Long organizationId;
    private String organizationName;

    private Long projectId;
    private String projectName;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    private String recordingFileUrl;    // FileObject → 공개 URL 변환 결과
}
