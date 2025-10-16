package com.azure.dto;

import com.azure.model.meeting.Meeting;
import com.azure.model.enums.MeetingStatus;
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
    private String audioUrl; // ✅ 프론트엔드에 전달할 최종 URL
    private MeetingStatus status;

    /**
     * Meeting 엔티티를 MeetingDTO로 변환합니다.
     * @param entity 변환할 Meeting 엔티티
     * @param s3BaseUrl S3 버킷의 기본 공개 URL (예: https://your-bucket.s3.ap-northeast-2.amazonaws.com)
     * @return 변환된 MeetingDTO
     */
    public static MeetingDTO fromEntity(Meeting entity, String s3BaseUrl) {
        if (entity == null) return null;

        MeetingDTO dto = new MeetingDTO();
        dto.setId(entity.getId());
        dto.setStartedAt(entity.getStartedAt());
        dto.setEndedAt(entity.getEndedAt());
        dto.setStatus(entity.getStatus());

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

        // ✅ 바로 이 부분입니다!
        // 엔티티의 recordingFile 관계를 따라가서 storageKey를 가져온 후,
        // 완전한 URL로 조립해서 DTO의 audioUrl 필드에 담아줍니다.
        if (entity.getRecordingFile() != null && s3BaseUrl != null) {
            String storageKey = entity.getRecordingFile().getStorageKey();
            // s3BaseUrl이 슬래시로 끝나지 않는 경우를 대비
            String baseUrl = s3BaseUrl.endsWith("/") ? s3BaseUrl : s3BaseUrl + "/";
            dto.setAudioUrl(baseUrl + storageKey);
        }

        return dto;
    }
}