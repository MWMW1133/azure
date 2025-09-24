package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="project_proposals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectProposal extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="proposer_id")
  private User proposer;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="department_id")
  private Department department;

  @Column(nullable=false, length=255)
  private String name;

  @Lob @Column(columnDefinition="longtext")
  private String description;

  @Column(length=32)
  private String status;
}
