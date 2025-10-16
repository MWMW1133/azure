package com.azure.model.meeting;

import com.azure.model.Organization;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.enums.MeetingStatus;
import com.azure.model.file.FileObject;
import com.azure.model.project.Project;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private ProjectCalendar event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    // 👇 JSON 관련 어노테이션 제거
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // ✅ 1. 회의 진행 상태 필드
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MeetingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recording_file")
    private FileObject recordingFile;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

