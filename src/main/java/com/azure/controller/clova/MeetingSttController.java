package com.azure.controller.clova;

import com.azure.service.clova.ClovaSpeechService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingSttController {
    private final ClovaSpeechService clova;

    @PostMapping("/{meetingId}/stt")
    public SubmitResp submit(@PathVariable Long meetingId, @RequestBody Req r) {
        var resp = clova.submitForMeeting(meetingId, r.audioUrl, r.diarization == null || r.diarization);
        return new SubmitResp(resp.getJobId(), resp.getStatus());
    }

    @Data public static class Req { private String audioUrl; private Boolean diarization; }
    public record SubmitResp(String jobId, String status) {}
}
