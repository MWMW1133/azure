package com.azure.model.calendar;

import com.azure.model.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "event_attendees",
       uniqueConstraints = @UniqueConstraint(name = "uq_event_user",
                                             columnNames = {"event_id", "user_id"}))
public class EventAttendee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private ProjectCalendar event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)

    private User user;

    @Column(length = 16)
    private String role;      // enum이면 @Enumerated(EnumType.STRING)

    @Column(length = 16)
    private String response;  // enum이면 @Enumerated(EnumType.STRING)

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

