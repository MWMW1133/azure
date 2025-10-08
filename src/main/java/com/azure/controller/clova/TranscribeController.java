// src/main/java/com/azure/controller/clova/TranscribeController.java
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transcripts")
public class TranscribeController {
    private final MeetingRepository meetingRepo;
    private final MeetingTranscriptRepository trRepo;
    private final MeetingSummaryRepository sumRepo;

    /** CLOVA 콜백: submit 시 ?meetingId=... 로 붙였으므로 여기서 식별 */
    @PostMapping("/callback")
    @Transactional
    public Map<String,Object> callback(@RequestParam Long meetingId, @RequestBody Map<String,Object> payload){
        Meeting m = meetingRepo.findById(meetingId).orElseThrow();

        String fullText = String.valueOf(payload.getOrDefault("text",""));      // 실제 응답에 맞춰 파싱 필요
        String summary  = String.valueOf(payload.getOrDefault("summary",""));
        String actions  = String.valueOf(payload.getOrDefault("actionItems",""));
        String lang     = "ko-KR";

        MeetingTranscript tr = new MeetingTranscript();
        tr.setMeeting(m); tr.setLang(lang);
        tr.setContent(trimTiny(fullText)); // ver5 TINYTEXT 보호
        trRepo.save(tr);

        MeetingSummary ms = new MeetingSummary();
        ms.setMeeting(m); ms.setSummaryMd(trimTiny(summary)); ms.setActionItems(trimTiny(actions));
        sumRepo.save(ms);

        return Map.of("ok", true);
    }

    private String trimTiny(String s){ if(s==null) return null; return s.length()>250? s.substring(0,250):s; }
}
