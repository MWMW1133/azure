package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "personal_calendars")
public class PersonalCalendar {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "personal_calendar_id") private Long personalCalendarId;
    private String title;
    private String description;
    @Column(name = "start_at") private LocalDateTime startAt;
    @Column(name = "end_at") private LocalDateTime endAt;
    @Column(name = "all_day") private Boolean allDay;
    private String rrule;
    private String location;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by")
    private User createdBy;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
}
