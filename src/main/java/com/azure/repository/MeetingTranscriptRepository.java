package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.meeting.MeetingTranscript;
import java.util.List;
import java.util.Optional;

public interface MeetingTranscriptRepository extends JpaRepository<MeetingTranscript, Long> {
    List<MeetingTranscript> findByMeeting_Id(Long meetingId);
    Optional<MeetingTranscript> findTopByMeeting_IdOrderByIdDesc(Long meetingId);
}
