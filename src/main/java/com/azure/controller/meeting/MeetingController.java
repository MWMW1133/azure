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

    @PostMapping("/{eventId}/start")
    public Meeting start(@PathVariable Long eventId,
                         @RequestParam Long organizationId,
                         @RequestParam Long projectId) {
        return meetingService.startMeeting(eventId, organizationId, projectId);
    }

    @PostMapping("/{meetingId}/end")
    public Meeting end(@PathVariable Long meetingId) {
        return meetingService.endMeeting(meetingId);
    }
}
