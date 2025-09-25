package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "reminders")
public class Reminder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_event_id")
    private ProjectCalendar projectEvent;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "personal_event_id")
    private PersonalCalendar personalEvent;
    @Column(name = "minutes_before") private Integer minutesBefore;
    private String method;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;
}
