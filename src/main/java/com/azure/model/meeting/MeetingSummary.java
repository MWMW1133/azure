// src/main/java/com/azure/model/meeting/MeetingSummary.java
package com.azure.model.meeting;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "meeting_summaries")
public class MeetingSummary {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 요약이 속한 회의
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    // [FIX] ver5 스키마(TINYTEXT) 제약을 명시
    @Lob
    @Column(name = "summary_md", columnDefinition = "TINYTEXT")
    private String summaryMd;

    // [FIX] ver5 스키마(TINYTEXT). JSON 문자열 보관 시 길이 주의
    @Lob
    @Column(name = "action_items", columnDefinition = "TINYTEXT")
    private String actionItems;
}
