package com.azure.model.meeting;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.project.Project;
import com.azure.model.file.FileObject;

@Data
@Entity
@Table(name = "meetings")
public class Meeting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private ProjectCalendar event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recording_file")
    private FileObject recordingFile;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
