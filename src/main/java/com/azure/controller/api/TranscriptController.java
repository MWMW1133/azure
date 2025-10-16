package com.azure.controller.api;

import com.azure.model.file.FileObject;
import com.azure.service.TranscriptService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transcripts")
public class TranscriptController {

    private final TranscriptService transcriptService;

    public record SubmitRequest(Long meetingId, String audioUrl, String mediaType, String lang) {}
    public record SaveRequest(String lang, String content) {}

    // ✅ JSON 바디로 오는 경우
    @PostMapping(value="/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,String>> submitJson(@RequestBody SubmitRequest req) {
        Long meetingId = req.meetingId();
        String url      = req.audioUrl();
        String media    = (req.mediaType() != null) ? req.mediaType() : "audio/webm";
        String lang     = (req.lang() != null) ? req.lang() : "ko-KR";

        transcriptService.handleSubmit(meetingId, url, media, lang); // <- 핵심!
        return ResponseEntity.accepted().body(Map.of("status","accepted"));
    }

    // (선택) x-www-form-urlencoded 로 올 때도 수용하고 싶으면 추가
    @PostMapping(value="/submit", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String,String>> submitForm(
            @RequestParam Long meetingId,
            @RequestParam String audioUrl,
            @RequestParam(required=false, defaultValue="audio/webm") String mediaType,
            @RequestParam(required=false, defaultValue="ko-KR") String lang
    ) {
        transcriptService.handleSubmit(meetingId, audioUrl, mediaType, lang);
        return ResponseEntity.accepted().body(Map.of("status","accepted"));
    }

    /**
     * 최종 수정된 회의록 내용을 저장하는 API (기존 기능)
     * POST /api/transcripts/{meetingId}/final
     */
    @PostMapping("/{meetingId}/final")
    public FileObject saveFinal(@PathVariable Long meetingId, @RequestBody SaveRequest request){
        Long uploaderId = 1L; // TODO: SecurityUtil 등을 사용해 현재 로그인 사용자 ID 가져오기
        return transcriptService.saveFinalTranscript(meetingId, uploaderId, request.lang(), request.content());
    }

    /**
     * 가장 최신 버전의 회의록 내용을 가져오는 API (기존 기능)
     * GET /api/transcripts/{meetingId}/latest
     */
    @GetMapping("/{meetingId}/latest")
    public String getLatest(@PathVariable Long meetingId){
        return transcriptService.getTranscriptContent(meetingId);
    }
}
