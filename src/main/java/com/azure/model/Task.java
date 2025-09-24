package com.azure.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="tasks", indexes=@Index(name="idx_task_project_rank", columnList="project_id,kanban_rank"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_id", nullable=false)
  private Project project;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="parent_task_id")
  private Task parent;

  @Column(nullable=false, length=200)
  private String title;

  @Lob @Column(columnDefinition="longtext")
  private String description;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="assignee_id")
  private User assignee;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="reporter_id")
  private User reporter;

  // PDF에는 컬럼명이 workflows_id로 표기됨
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="workflows_id")
  private WorkflowStep workflow;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="priority_id")
  private Priority priority;

  @Column(name="start_date")
  private LocalDate startDate;

  @Column(name="due_date")
  private LocalDate dueDate;

  @Column(name="completed_at")
  private LocalDateTime completedAt;

  @Column(name="progress_pct", precision=5, scale=2)
  private BigDecimal progressPct;

  @Column(name="kanban_rank", precision=18, scale=6)
  private BigDecimal kanbanRank;
}
