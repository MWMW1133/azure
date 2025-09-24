package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="chat_channels", indexes=@Index(name="idx_channel_project", columnList="project_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatChannel extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_id")
  private Project project;

  @Column(nullable=false, length=120)
  private String name;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by")
  private User createdBy;
}
