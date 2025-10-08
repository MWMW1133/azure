// src/main/java/com/azure/repository/MeetingTranscriptRepository.java
package com.azure.repository;

import com.azure.model.meeting.MeetingTranscript;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MeetingTranscriptRepository extends JpaRepository<MeetingTranscript, Long> {

    // 특정 미팅의 모든 전사
    List<MeetingTranscript> findByMeeting_Id(Long meetingId);

    // [FIX] 엔티티에 meetingId 필드가 없으므로 경로 기반으로 수정
    Optional<MeetingTranscript> findTopByMeeting_IdOrderByIdDesc(Long meetingId);
}
