package com.azure.controller.api;

import com.azure.model.meeting.MeetingSummary;
import com.azure.service.MeetingMinutesService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingMinutesApiController {

    private final MeetingMinutesService minutesService;

    /** 전사 기반 회의록 생성/갱신 */
    @PostMapping("/{meetingId}/summarize")
    public ResponseEntity<SummaryResp> summarize(@PathVariable Long meetingId) {
        MeetingSummary saved = minutesService.summarizeAndSave(meetingId);
        SummaryResp resp = new SummaryResp();
        resp.setMeetingId(meetingId);
        resp.setSummaryMd(saved.getSummaryMd());
        return ResponseEntity.ok(resp);
    }

    /** 최신 회의록 조회 */
    @GetMapping("/{meetingId}/summaries/latest")
    public ResponseEntity<SummaryResp> latest(@PathVariable Long meetingId) {
        MeetingSummary s = minutesService.getLatest(meetingId);
        if (s == null) return ResponseEntity.notFound().build();
        SummaryResp resp = new SummaryResp();
        resp.setMeetingId(meetingId);
        resp.setSummaryMd(s.getSummaryMd());
        return ResponseEntity.ok(resp);
    }

    @Data
    public static class SummaryResp {
        private Long meetingId;
        private String summaryMd;
    }
}
