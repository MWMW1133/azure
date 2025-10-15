package com.azure.controller.meeting;

import com.azure.dto.MeetingDTO;
import com.azure.model.meeting.Meeting;
import com.azure.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingApiController {
    private final MeetingService meetingService;

    public record StartReq(Long organizationId, Long projectId) {}

    @PostMapping("/start")
    public ResponseEntity<MeetingDTO> start(@RequestBody StartReq req) {
        Meeting meetingEntity = meetingService.startMeeting(req.organizationId(), req.projectId());
        // 엔티티를 DTO로 변환하여 반환
        return ResponseEntity.ok(MeetingDTO.fromEntity(meetingEntity));
    }

    @PostMapping("/{meetingId}/end")
    public ResponseEntity<MeetingDTO> end(@PathVariable Long meetingId) {
        Meeting meetingEntity = meetingService.endMeeting(meetingId);
        // 엔티티를 DTO로 변환하여 반환
        return ResponseEntity.ok(MeetingDTO.fromEntity(meetingEntity));
    }
}

