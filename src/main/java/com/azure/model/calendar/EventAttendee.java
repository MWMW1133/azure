package com.azure.model.calendar;

import jakarta.persistence.*;
import lombok.Data;
import com.azure.model.user.User;

@Data
@Entity
@Table(name="event_attendees",
        uniqueConstraints=@UniqueConstraint(columnNames={"event_id","user_id"}))
public class EventAttendee {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="event_id", nullable=false)
    private ProjectCalendar event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(length = 16)
    private String role;      // ORGANIZER, MEMBER ...

    @Column(length = 16)
    private String response;  // ACCEPTED, DECLINED, TENTATIVE ...
}