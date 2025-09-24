package com.azure.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="meetings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Meeting extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="event_id")
  private ProjectCalendarEvent event;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_id")
  private Project project;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="recording_file")
  private FileObject recordingFile;

  @Column(name="started_at")
  private LocalDateTime startedAt;

  @Column(name="ended_at")
  private LocalDateTime endedAt;
}
