package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.MeetingSummary;
import java.util.List;

public interface MeetingSummaryRepository extends JpaRepository<MeetingSummary, Long> {
    List<MeetingSummary> findByMeetingId(Long meetingId);
}
