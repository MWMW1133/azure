package com.azure.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingDTO {
    private Long id;
    private Long eventId;
    private String meetingName;
    private String meetingDescription;
    private Long organizationId;
    private String organizationName;
    private Long projectId;
    private String projectName;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String recordingFileUrl;
}
