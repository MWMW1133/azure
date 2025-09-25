package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "meeting_summaries")
public class MeetingSummary {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "meeting_id")
    private Meeting meeting;
    @Column(name = "summary_md") private String summaryMd;
    @Column(name = "action_items") private String actionItems; // JSON string
}
