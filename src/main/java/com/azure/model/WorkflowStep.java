package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="workflows", indexes=@Index(name="idx_workflow_project", columnList="project_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkflowStep extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_id", nullable=false)
  private Project project;

  @Column(nullable=false, length=100)
  private String name;

  @Column(name="wip_limit")
  private Integer wipLimit;
}
