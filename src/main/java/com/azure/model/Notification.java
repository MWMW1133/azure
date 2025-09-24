package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="notifications", indexes=@Index(name="idx_notif_user", columnList="user_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id")
  private User user;

  @Column(length=120)
  private String type;

  @Lob @Column(columnDefinition="longtext")
  private String data;

  @Column(name="is_read")
  private Boolean read;
}
