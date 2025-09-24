package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="projects")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Project extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false, length=200)
  private String name;

  @Lob @Column(columnDefinition="longtext")
  private String description;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="owner_id")
  private User owner;

  @Column(name="start_date")
  private java.time.LocalDate startDate;

  @Column(name="due_date")
  private java.time.LocalDate dueDate;
}
