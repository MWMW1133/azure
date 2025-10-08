// src/main/java/com/azure/model/meeting/MeetingTranscript.java
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

    /** [FIX] ver5 스키마: content가 TINYTEXT였던 점 반영
     *  전문을 저장하려면 TEXT/MEDIUMTEXT로 스키마 변경 권장 */
    @Lob
    @Column(name = "content", columnDefinition = "TINYTEXT")
    private String content;
}
