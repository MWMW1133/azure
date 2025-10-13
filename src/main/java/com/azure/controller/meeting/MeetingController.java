package com.azure.controller.meeting;

import com.azure.model.meeting.Meeting;
import com.azure.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingController {
    private final MeetingService meetingService;

    @PostMapping("/start")
    public StartResp start(@RequestParam Long organizationId,
                           @RequestParam Long projectId) {
        Meeting m = meetingService.startMeeting(organizationId, projectId);
        String channel = "org_%d_proj_%d".formatted(organizationId, projectId);
        return new StartResp(m.getId(), channel);
    }

    @PostMapping("/{meetingId}/end")
    public Meeting end(@PathVariable Long meetingId) {
        return meetingService.endMeeting(meetingId);
    }

    public record StartResp(Long meetingId, String channel) {}
}
