package com.azure.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="project_calendars")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectCalendarEvent extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_id")
  private Project project;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="related_task_id")
  private Task relatedTask;

  @Column(nullable=false, length=200)
  private String title;

  @Column(name="start_at")
  private LocalDateTime startAt;

  @Column(name="end_at")
  private LocalDateTime endAt;

  @Column(length=20)
  private String status;
}
