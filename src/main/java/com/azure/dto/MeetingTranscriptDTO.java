// src/main/java/com/azure/dto/MeetingTranscriptDTO.java
package com.azure.dto;

import lombok.Data;

@Data
public class MeetingTranscriptDTO {
    private Long id;
    private Long meetingId;
    private String lang;
    private String content; // TINYTEXT 제약: 전문이 길면 잘림
}
