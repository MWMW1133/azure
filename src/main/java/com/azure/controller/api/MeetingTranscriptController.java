package com.azure.controller.api;

import com.azure.model.enums.MeetingStatus;
import com.azure.model.meeting.Meeting;
import com.azure.repository.MeetingRepository;
import com.azure.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingTranscriptController {

    private final TranscriptService transcriptService;
    private final MeetingRepository meetingRepository;

    // 회의 전사 최신본 조회
    @GetMapping("/{id}/transcript")
    public ResponseEntity<TranscriptDto> getTranscript(@PathVariable Long id) {
        String content = transcriptService.getTranscriptContent(id);          // ""(빈문자)일 수 있음
        MeetingStatus status = meetingRepository.findById(id)
                .map(Meeting::getStatus)
                .orElse(MeetingStatus.SCHEDULED);
        // lang은 DB에 저장한 값이 따로 있으면 그 값으로 바꿔도 됨
        return ResponseEntity.ok(new TranscriptDto(status.name(), "ko-KR", content));
    }

    // 모달에서 최종 편집본 저장(선택)
    @PostMapping("/{id}/transcript/final")
    public ResponseEntity<?> saveFinal(@PathVariable Long id,
                                       @RequestBody SaveReq req) {
        var fo = transcriptService.saveFinalTranscript(id, req.uploaderId(), req.lang(), req.content());
        return ResponseEntity.ok(new SaveRes(fo.getId(), fo.getFileName()));
    }

    // --- DTO ---
    public record TranscriptDto(String status, String lang, String content) {}
    public record SaveReq(Long uploaderId, String lang, String content) {}
    public record SaveRes(Long fileObjectId, String fileName) {}
}
