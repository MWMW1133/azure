package com.azure.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MeetingDTO {
    private Long id;
    private Long eventId;           // project_event_id
    private Long organizationId;    // organization_id
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Long recordingFileId;   // file_objects.id
}
