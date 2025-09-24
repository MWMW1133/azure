package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="audit_logs", indexes=@Index(name="idx_audit_actor", columnList="actor_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="actor_id")
  private User actor;

  @Column(length=120)
  private String action;

  @Lob @Column(columnDefinition="longtext")
  private String detail;
}
