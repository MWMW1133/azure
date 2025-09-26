package com.azure.model.calendar;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.user.User;

/**
 * 개인 캘린더 엔티티.
 * - is_done 컬럼(Boolean) 추가: 기본값 false
 * - MySQL TINYINT(1)과 JPA Boolean은 자동 매핑됨.
 */
@Data
@Entity
@Table(name = "personal_calendars")
public class PersonalCalendar {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "personal_calendar_id")
    private Long personalCalendarId;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob @Column
    private String description;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "all_day")
    private Boolean allDay;

    @Column(name = "rrule", length = 200)
    private String rrule;

    @Column(name = "location", length = 200)
    private String location;

    /** 완료 여부 (DB TINYINT(1) 기본 false). */
    @Column(name = "is_done", nullable = false)
    private Boolean isDone = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
