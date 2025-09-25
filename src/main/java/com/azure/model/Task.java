package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "tasks")
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assignee_id")
    private User assignee;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "workflows_id")
    private Workflow workflow;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "priority_id")
    private Priority priority;

    @Column(name = "start_date") private LocalDate startDate;
    @Column(name = "due_date") private LocalDate dueDate;
    @Column(name = "completed_at") private LocalDateTime completedAt;
    @Column(name = "progress_pct") private java.math.BigDecimal progressPct;
    @Column(name = "kanban_rank") private java.math.BigDecimal kanbanRank;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
}
