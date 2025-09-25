package com.azure.model.meeting;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "meeting_transcripts")
public class MeetingTranscript {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @Column(name = "lang", length = 20)
    private String lang;

    @Lob @Column(name = "content")
    private String content;
}
