package com.azure.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MeetingDTO {
    private Long id;
    private Long eventId;          // meetings.event_id
    private Long organizationId;   // meetings.organization_id
    private Long projectId;        // meetings.project_id
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Long recordingFileId;  // meetings.recording_file (file_objects.id)
    private LocalDateTime createdAt;
}
