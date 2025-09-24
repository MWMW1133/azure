package com.azure.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

public class EventAttendeeDTO {
    private Long projectCalendarId;
    private Long userId;
    private String role;
    private String response;


    public Long getProjectCalendarId() {
        return projectCalendarId;
    }

    public void setProjectCalendarId(Long projectCalendarId) {
        this.projectCalendarId = projectCalendarId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
