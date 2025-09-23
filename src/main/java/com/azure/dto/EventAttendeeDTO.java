package com.azure.dto;


public class EventAttendeeDTO {
    private int projectCalendarId;
    private int userId;
    private String role;
    private String response;


    public int getProjectCalendarId() {
        return projectCalendarId;
    }

    public void setProjectCalendarId(int projectCalendarId) {
        this.projectCalendarId = projectCalendarId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
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
