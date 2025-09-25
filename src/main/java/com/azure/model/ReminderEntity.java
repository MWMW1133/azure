package com.azure.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
@IdClass(ReminderId.class)
public class ReminderEntity {

    @Id
    @Column(name = "project_calendar_id")
    private Long projectCalendarId;

    @Id
    @Column(name = "minutes_before")
    private Integer minutesBefore;

    @Column(name = "method")
    private String method;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getter/Setter
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
