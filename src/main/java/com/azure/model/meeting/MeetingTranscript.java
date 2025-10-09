package com.azure.model.meeting;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "meeting_transcripts")
public class MeetingTranscript {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 전사가 속한 회의 (FK, ON DELETE CASCADE) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    /** 언어 코드(예: "ko-KR", "en-US") */
    @Column(name = "lang", length = 20)
    private String lang;

    /** 전사 본문 — TINYTEXT/LONGTEXT 대응을 위해 @Lob 권장 */
    @Lob
    @Column(name = "content")
    private String content;
}
