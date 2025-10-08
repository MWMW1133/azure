// src/main/java/com/azure/repository/MeetingSummaryRepository.java
package com.azure.repository;

import com.azure.model.meeting.MeetingSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MeetingSummaryRepository extends JpaRepository<MeetingSummary, Long> {

    // 특정 회의의 요약 목록
    List<MeetingSummary> findByMeeting_Id(Long meetingId);

    // [옵션] 최신 1건
    Optional<MeetingSummary> findTopByMeeting_IdOrderByIdDesc(Long meetingId);
}
