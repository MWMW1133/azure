package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="documents", indexes=@Index(name="idx_doc_project", columnList="progitject_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_id", nullable=false)
  private Project project;

  @Column(nullable=false, length=200)
  private String title;

  @Column(name="template_key", length=120)
  private String templateKey;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="author_id")
  private User author;
}
