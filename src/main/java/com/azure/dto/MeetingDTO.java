package com.azure.dto;

import com.azure.model.meeting.Meeting;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MeetingDTO {
    private Long id;
    private Long eventId;
    private String meetingName;
    private Long organizationId;
    private Long projectId;
    private String projectName;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String recordingFileUrl;

    // Meeting 엔티티를 MeetingDTO로 변환하는 정적 팩토리 메서드
    public static MeetingDTO fromEntity(Meeting entity) {
        if (entity == null) return null;

        MeetingDTO dto = new MeetingDTO();
        dto.setId(entity.getId());
        dto.setStartedAt(entity.getStartedAt());
        dto.setEndedAt(entity.getEndedAt());

        if (entity.getEvent() != null) {
            dto.setEventId(entity.getEvent().getId());
            dto.setMeetingName(entity.getEvent().getTitle());
        }

        if (entity.getOrganization() != null) {
            dto.setOrganizationId(entity.getOrganization().getId());
        }

        if (entity.getProject() != null) {
            dto.setProjectId(entity.getProject().getId());
            dto.setProjectName(entity.getProject().getName());
        }

        if (entity.getRecordingFile() != null) {
            // TODO: S3 전체 URL을 조합하는 로직이 필요하다면 여기에 추가
            dto.setRecordingFileUrl(entity.getRecordingFile().getStorageKey());
        }

        return dto;
    }
}
