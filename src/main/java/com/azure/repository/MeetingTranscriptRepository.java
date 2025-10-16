package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.meeting.MeetingTranscript;
import java.util.List;
import java.util.Optional;

public interface MeetingTranscriptRepository extends JpaRepository<MeetingTranscript, Long> {
    // 특정 미팅의 모든 녹취록 조회
    List<MeetingTranscript> findByMeeting_Id(Long meetingId);
    // 특정 미팅의 녹취록 단일 조회
    Optional<MeetingTranscript> findByMeetingId(Long meetingId);

}
