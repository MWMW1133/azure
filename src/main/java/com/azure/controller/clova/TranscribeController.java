package com.azure.controller.clova;

import com.azure.model.meeting.Meeting;
import com.azure.model.meeting.MeetingSummary;
import com.azure.model.meeting.MeetingTranscript;
import com.azure.repository.MeetingRepository;
import com.azure.repository.MeetingSummaryRepository;
import com.azure.repository.MeetingTranscriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transcripts")
public class TranscribeController {
    private final MeetingRepository meetingRepo;
    private final MeetingTranscriptRepository trRepo;
    private final MeetingSummaryRepository sumRepo;

    /** CLOVA 콜백 (?meetingId=) */
    @PostMapping("/callback")
    @Transactional
    public Map<String,Object> callback(@RequestParam Long meetingId,
                                       @RequestBody Map<String,Object> payload){
        Meeting m = meetingRepo.findById(meetingId).orElseThrow();

        // 실제 응답 필드명에 맞게 조정
        String fullText = Objects.toString(payload.get("text"), "");
        String summary  = Objects.toString(payload.get("summary"), "");
        String actions  = Objects.toString(payload.get("actionItems"), "");
        String lang     = "ko-KR";

        var tr = new MeetingTranscript();
        tr.setMeeting(m); tr.setLang(lang); tr.setContent(fullText);
        trRepo.save(tr);

        var ms = new MeetingSummary();
        ms.setMeeting(m); ms.setSummaryMd(summary); ms.setActionItems(actions);
        sumRepo.save(ms);

        return Map.of("ok", true);
    }
}
