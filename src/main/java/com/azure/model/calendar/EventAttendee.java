package com.azure.model.calendar;

import jakarta.persistence.*;
import lombok.Data;
import com.azure.model.project.Project;
import com.azure.model.user.User;

@Data
@Entity
@Table(name = "event_attendees")
public class EventAttendee {
    @EmbeddedId
    private EventAttendeeId id;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 16)
    private String role;

    @Column(length = 16)
    private String response;
}
