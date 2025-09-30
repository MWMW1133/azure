package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PersonalCalendarDTO {
    private Long id;
    private Long personalCalendarId;
    private String title;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Boolean allDay;
    private Boolean isDone;
    private String rrule;
    private String location;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
