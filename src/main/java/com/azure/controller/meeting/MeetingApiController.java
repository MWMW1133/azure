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
    // ❌ 서비스 계층으로 이동했으므로 컨트롤러에서는 S3 URL이 더 이상 필요 없음

    public record StartReq(Long organizationId, Long projectId) {}

    @PostMapping("/start")
    public ResponseEntity<MeetingDTO> start(@RequestBody StartReq req) {
        // ✅ 서비스가 직접 DTO를 반환하므로 바로 받아서 사용
        MeetingDTO meetingDTO = meetingService.startMeeting(req.organizationId(), req.projectId());
        return ResponseEntity.ok(meetingDTO);
    }

    @PostMapping("/{meetingId}/end")
    public ResponseEntity<MeetingDTO> end(@PathVariable Long meetingId) {
        MeetingDTO meetingDTO = meetingService.endMeeting(meetingId);
        return ResponseEntity.ok(meetingDTO);
    }
}

