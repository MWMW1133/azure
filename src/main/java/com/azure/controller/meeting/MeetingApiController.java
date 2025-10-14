package com.azure.controller.meeting;

import com.azure.model.meeting.Meeting;
import com.azure.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingApiController {

    private final MeetingService meetingService;

    public record StartReq(Long organizationId, Long projectId) {}
    public record StartResp(Long meetingId, String channel) {}

    @PostMapping("/start")
    public StartResp start(@RequestBody StartReq req) {
        if (req.projectId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "projectId is required");
        }
        Meeting m = meetingService.startMeeting(req.organizationId(), req.projectId());
        String channel = "org_%d_proj_%d".formatted(
                req.organizationId() == null ? 0 : req.organizationId(),
                req.projectId()
        );
        return new StartResp(m.getId(), channel);
    }

    @PostMapping("/{meetingId}/end")
    public Meeting end(@PathVariable Long meetingId) {
        return meetingService.endMeeting(meetingId);
    }
}
