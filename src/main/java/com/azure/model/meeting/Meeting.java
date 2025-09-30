package com.azure.model.meeting;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.file.FileObject;
import com.azure.model.Organization;

@Data
@Entity
@Table(name = "meetings")
public class Meeting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 회의가 연결된 프로젝트 이벤트 (NOT NULL) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private ProjectCalendar event;

    /** 소속 조직 (NULL 허용, SET NULL) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    /** 회의 시작/종료 시각 */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    /** 녹화 파일(선택) — file_objects.id */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recording_file")
    private FileObject recordingFile;
}
