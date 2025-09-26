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

    // 회의가 연결된 프로젝트 이벤트 (FK: meetings.event_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private ProjectCalendar event;

    // 소속 프로젝트 (FK: meetings.project_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    // 회의 시작/종료 시각
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // 녹화 파일(선택). 파일 메타(file_objects)와 연계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recording_file")
    private FileObject recordingFile;

    // [주의] DB에서 DEFAULT CURRENT_TIMESTAMP 로 생성된다면
    // @Column(name = "created_at", insertable = false, updatable = false) 로 두는 걸 권장
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

