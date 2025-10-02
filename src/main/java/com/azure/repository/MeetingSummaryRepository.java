package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.meeting.MeetingSummary;
import java.util.List;

public interface MeetingSummaryRepository extends JpaRepository<MeetingSummary, Long> {

    // ★ 특정 회의의 요약 목록
    List<MeetingSummary> findByMeeting_Id(Long meetingId);
}
