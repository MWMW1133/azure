package com.azure.controller.meeting;

import com.azure.model.meeting.Meeting;
import com.azure.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meetings") // API 경로는 그대로 /api/meetings
@RequiredArgsConstructor
public class MeetingApiController {
    private final MeetingService meetingService;

    // 회의 시작 API
// 1. 요청 데이터를 받을 DTO 클래스를 만듭니다. (레코드 record를 사용하면 간편합니다)
    public record StartReq(Long organizationId, Long projectId) {}

    @PostMapping("/start")
    public StartResp start(@RequestBody StartReq req) {
        Meeting m = meetingService.startMeeting(req.organizationId(), req.projectId());
        String channel = "org_%d_proj_%d".formatted(req.organizationId(), req.projectId());
        return new StartResp(m.getId(), channel);
    }

    // 회의 종료 API
    @PostMapping("/{meetingId}/end")
    public Meeting end(@PathVariable Long meetingId) {
        return meetingService.endMeeting(meetingId);
    }

    public record StartResp(Long meetingId, String channel) {}
}