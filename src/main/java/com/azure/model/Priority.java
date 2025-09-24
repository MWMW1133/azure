package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="priorities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Priority extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false, length=32)
  private String name;

  @Column(name="level")
  private Short level;
}
