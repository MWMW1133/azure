package com.azure.dto;
import lombok.Data;

@Data
public class MeetingSummaryDTO {
    private Long id;
    private Long meetingId;
    private String summaryMd;
    private String actionItems;
}
