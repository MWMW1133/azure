package com.azure.controller.api;

import com.azure.model.file.FileObject;
import com.azure.service.TranscriptService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class TranscriptController {

    private final TranscriptService transcriptService;

    @PostMapping("/{id}/transcripts/final")
    public FileObject saveFinal(@PathVariable Long id, @RequestBody SaveReq r){
        Long uploader = 1L; // TODO: 로그인 사용자 ID
        return transcriptService.saveFinalTranscript(id, uploader, r.lang, r.content);
    }

    @GetMapping("/{id}/transcripts/latest")
    public String latest(@PathVariable Long id){
        return transcriptService.getTranscriptContent(id);
    }

    @Data public static class SaveReq { private String lang; private String content; }
}
