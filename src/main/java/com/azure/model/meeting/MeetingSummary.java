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

    // 마크다운 본문
    @Lob @Column(name = "summary_md")
    private String summaryMd;

    // 액션아이템(JSON String). 필요 시 @Convert로 JSON 매핑 가능
    @Lob @Column(name = "action_items")
    private String actionItems;
}

