package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectCalendarDTO {
    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Boolean allDay;
    private String rrule;
    private String location;
    private String color;
    private Long relatedTaskId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
