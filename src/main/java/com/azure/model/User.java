package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="users", indexes = @Index(name="idx_users_email", columnList="email", unique = true))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false, length=255)
  private String email;

  @Column(name="password_hash", nullable=false, length=255)
  private String passwordHash;

  @Column(nullable=false, length=80)
  private String name;

  @Column(name="avatar_url", length=500)
  private String avatarUrl;
}
