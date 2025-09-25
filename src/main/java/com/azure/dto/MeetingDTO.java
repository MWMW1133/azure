package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MeetingDTO {
    private Long id;
    private Long eventId;
    private Long projectId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Long recordingFile;
    private LocalDateTime createdAt;
}
