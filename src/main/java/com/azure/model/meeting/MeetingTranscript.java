package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "meeting_transcripts")
public class MeetingTranscript {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "meeting_id")
    private Meeting meeting;
    private String lang;
    @Column(columnDefinition = "TEXT") private String content;
}
