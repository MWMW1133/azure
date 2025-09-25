package com.azure.model;


import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Data;
@Data @Embeddable
public class EventAttendeeId implements Serializable {
    private Long projectId; // points to project_calendars(project_id)
    private Long userId;
}
