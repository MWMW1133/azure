package com.azure.model.meeting;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "meeting_summaries")
public class MeetingSummary {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @Lob @Column(name = "summary_md")
    private String summaryMd;

    @Lob @Column(name = "action_items")
    private String actionItems; // JSON string
}
