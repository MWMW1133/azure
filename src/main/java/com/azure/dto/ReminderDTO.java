package com.azure.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

public class ReminderDTO {
    private Long projectCalendarId;
    private Integer minutesBefore;
    private String method;


    public Long getProjectCalendarId() {
        return projectCalendarId;
    }

    public void setProjectCalendarId(Long projectCalendarId) {
        this.projectCalendarId = projectCalendarId;
    }

    public Integer getMinutesBefore() {
        return minutesBefore;
    }

    public void setMinutesBefore(Integer minutesBefore) {
        this.minutesBefore = minutesBefore;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
