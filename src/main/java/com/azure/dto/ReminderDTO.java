package com.azure.dto;


public class ReminderDTO {
    private int projectCalendarId;
    private int minutesBefore;
    private String method;


    public int getProjectCalendarId() {
        return projectCalendarId;
    }

    public void setProjectCalendarId(int projectCalendarId) {
        this.projectCalendarId = projectCalendarId;
    }

    public int getMinutesBefore() {
        return minutesBefore;
    }

    public void setMinutesBefore(int minutesBefore) {
        this.minutesBefore = minutesBefore;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
