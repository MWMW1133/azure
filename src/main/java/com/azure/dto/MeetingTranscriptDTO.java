package com.azure.dto;
import lombok.Data;

@Data
public class MeetingTranscriptDTO {
    private Long id;
    private Long meetingId;
    private String lang;
    private String content;
}
