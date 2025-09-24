package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="meeting_transcripts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MeetingTranscript extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="meeting_id")
  private Meeting meeting;

  @Lob @Column(columnDefinition="longtext")
  private String text;
}
