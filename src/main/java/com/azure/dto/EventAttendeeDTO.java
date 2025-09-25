package com.azure.dto;
import lombok.Data;

@Data
public class EventAttendeeDTO {
    private Long projectId;
    private Long userId;
    private String role;
    private String response;
}
