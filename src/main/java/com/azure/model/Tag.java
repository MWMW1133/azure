package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="tags",
  uniqueConstraints=@UniqueConstraint(name="uq_tags_project_name", columnNames={"project_id","name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tag extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_id", nullable=false)
  private Project project;

  @Column(nullable=false, length=60)
  private String name;
}
