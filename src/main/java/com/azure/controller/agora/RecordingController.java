package com.azure.controller.agora;

import com.azure.service.CloudRecordingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recordings")
public class RecordingController {
    private final CloudRecordingService cloudRecordingService;

    @PostMapping("/start")
    public StartResp start(@RequestBody StartReq req) {
        var s = cloudRecordingService.start(req.eventId, req.uid);
        return new StartResp(s.resourceId(), s.sid(), s.channel());
    }

    @PostMapping("/stop")
    public void stop(@RequestBody StopReq req) {
        cloudRecordingService.stop(req.meetingId, req.channel, req.uid, req.resourceId, req.sid);
    }

    @Data public static class StartReq { private Long eventId; private String uid; }
    @Data public static class StopReq {
        private Long meetingId; private String channel; private String uid; private String resourceId; private String sid;
    }
    public record StartResp(String resourceId, String sid, String channel) { }
}
