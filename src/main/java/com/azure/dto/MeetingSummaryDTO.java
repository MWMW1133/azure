// src/main/java/com/azure/dto/MeetingSummaryDTO.java
package com.azure.dto;

import lombok.Data;

@Data
public class MeetingSummaryDTO {
    private Long id;
    private Long meetingId;
    private String summaryMd;     // TINYTEXT 제약: 길이 주의
    private String actionItems;   // TINYTEXT 제약: 길이 주의
}
