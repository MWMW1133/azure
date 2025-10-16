package com.azure.service;

import com.azure.model.file.FileObject;

public interface TranscriptService {
    // ✅ 추가: 프론트(JS 수정 없이) 제출된 webm을 서버에서 처리
    void handleSubmit(Long meetingId, String audioUrl, String mediaType, String lang);

    FileObject saveFinalTranscript(Long meetingId, Long uploaderId, String lang, String content);
    String getTranscriptContent(Long meetingId);
}
