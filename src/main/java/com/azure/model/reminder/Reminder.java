package com.azure.model.reminder;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.calendar.PersonalCalendar;
import com.azure.model.user.User;

@Data
@Entity
@Table(name = "reminders")
public class Reminder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_event_id")
    private ProjectCalendar projectEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_event_id")
    private PersonalCalendar personalEvent;

    @Column(name = "minutes_before", nullable = false)
    private Integer minutesBefore;

    @Column(name = "method", nullable = false, length = 255)
    private String method;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
