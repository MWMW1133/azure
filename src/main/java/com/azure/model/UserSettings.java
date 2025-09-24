package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="user_settings", uniqueConstraints=@UniqueConstraint(name="uq_settings_user", columnNames={"user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserSettings extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
  private User user;

  @Column(name="tz", length=64)
  private String timeZone;

  @Column(name="lang", length=10)
  private String language;
}
