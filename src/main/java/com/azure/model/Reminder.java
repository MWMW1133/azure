package com.azure.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="reminders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reminder extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_calendar_id")
  private ProjectCalendarEvent event;

  @Column(name="remind_at")
  private LocalDateTime remindAt;

  @Column(length=120)
  private String method; // email/push
}
