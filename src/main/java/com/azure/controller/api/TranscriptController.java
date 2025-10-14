package com.azure.controller.api;

import com.azure.model.file.FileObject;
import com.azure.service.TranscriptService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
// ❗️ API 기본 주소를 /api/transcripts 로 변경합니다.
@RequestMapping("/api/transcripts")
public class TranscriptController {

    private final TranscriptService transcriptService;

    // --- DTO 클래스 정의 ---
    // 프론트에서 보낼 데이터 (STT 제출용)
    public record SubmitRequest(Long meetingId, String audioUrl, String mediaType) {}
    // 프론트에서 보낼 데이터 (최종본 저장용)
    public record SaveRequest(String lang, String content) {}


    /**
     * STT(음성-텍스트 변환) 작업을 위해 S3 URL을 제출받는 API
     * 프론트의 POST /api/transcripts/submit 요청을 처리합니다.
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitForTranscription(@RequestBody SubmitRequest request) {

        System.out.println("✅ /api/transcripts/submit API 호출 성공!");
        System.out.println(" - Meeting ID: " + request.meetingId());
        System.out.println(" - S3 Audio URL: " + request.audioUrl());

        // TODO: 여기에 실제 Naver Clova STT API를 호출하는 로직을 구현해야 합니다.
        // transcriptService.startTranscription(request.meetingId(), request.audioUrl());

        // 우선 성공 응답을 반환하여 프론트엔드 에러를 막습니다.
        return ResponseEntity.ok(Map.of("status", "success", "message", "Transcription job submitted."));
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
