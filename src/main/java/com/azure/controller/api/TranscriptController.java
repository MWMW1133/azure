package com.azure.controller.api;

import com.azure.model.file.FileObject;
import com.azure.service.TranscriptService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 녹음 종료 시, 프론트가 최종 텍스트를 POST하는 엔드포인트.
 * → 파일 저장 + file_objects/meeting_transcripts 기록 후 FileObject 정보를 응답.
 */
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class TranscriptController {

    private final TranscriptService transcriptService;

    /** POST /api/meetings/{meetingId}/transcripts/final */
    @PostMapping("/{meetingId}/transcripts/final")
    public SaveTranscriptResp save(@PathVariable Long meetingId, @RequestBody SaveTranscriptReq req) {
        FileObject fo = transcriptService.saveFinalTranscript(
                meetingId, req.getUploaderId(), req.getLang(), req.getContent());

        SaveTranscriptResp resp = new SaveTranscriptResp();
        resp.setFileObjectId(fo.getId());
        resp.setFileName(fo.getFileName());
        return resp;
    }

    /** 요청 바디 DTO */
    @Data public static class SaveTranscriptReq {
        private Long uploaderId;   // 현재 로그인 사용자 ID
        private String lang;       // "ko-KR"
        private String content;    // 최종 전사 텍스트
    }
    /** 응답 DTO */
    @Data public static class SaveTranscriptResp {
        private Long fileObjectId;
        private String fileName;
    }
}
