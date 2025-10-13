package com.azure.dto;

import lombok.Data;

@Data
public class EventAttendeeDTO {
    private Long eventId;   // ✅ projectId → eventId
    private Long userId;
    private String role;      // ORGANIZER / MEMBER ...
    private String response;  // ACCEPTED / DECLINED / TENTATIVE ...
}