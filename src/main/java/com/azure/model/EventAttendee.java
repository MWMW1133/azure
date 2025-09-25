package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "event_attendees")
public class EventAttendee {
    @EmbeddedId private EventAttendeeId id;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("userId") @JoinColumn(name = "user_id")
    private User user;
    private String role;
    private String response;
    // NOTE: projectId -> project_calendars(project_id) is not PK; keep scalar in id only.
}
