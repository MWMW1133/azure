package com.azure.dto;
import lombok.Data;

@Data
public class EventAttendeeDTO {
    private Long eventId;   // ⬅️ projectId → eventId로 변경
    private Long userId;
    private String role;
    private String response;
}

