package com.azure.service;

import com.azure.model.file.FileObject;

public interface TranscriptService {
    FileObject saveFinalTranscript(Long meetingId, Long uploaderId, String lang, String content);
    String getTranscriptContent(Long meetingId);
}
