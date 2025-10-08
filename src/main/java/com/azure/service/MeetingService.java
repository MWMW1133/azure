// src/main/java/com/azure/service/MeetingService.java
package com.azure.service;

import com.azure.dto.MeetingDTO;
import com.azure.model.meeting.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface MeetingService {
    // [REPLACE] 이벤트 기반 시작/종료
    Meeting startMeeting(Long eventId, Long organizationId, Long projectId, LocalDateTime startedAt);
    Meeting endMeeting(Long meetingId, LocalDateTime endedAt);

    MeetingDTO create(MeetingDTO dto);
    MeetingDTO get(Long meetingId);

    Page<MeetingDTO> listByOrganization(Long organizationId, Pageable pageable);
    Page<MeetingDTO> listByProject(Long projectId, Pageable pageable);
    Page<MeetingDTO> listByOrganizationAndProject(Long organizationId, Long projectId, Pageable pageable);
}
