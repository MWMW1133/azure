// src/main/java/com/azure/service/meeting/RecordingFinalizeService.java
package com.azure.service.meeting;

import com.azure.config.AgoraProps;
import com.azure.model.file.FileObject;
import com.azure.model.meeting.Meeting;
import com.azure.repository.FileObjectRepository;
import com.azure.repository.MeetingRepository;
import com.azure.service.clova.ClovaSpeechService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordingFinalizeService {
    private final MeetingRepository meetingRepo;
    private final FileObjectRepository fileRepo;
    private final ClovaSpeechService clova;
    private final AgoraProps props;
    private final ObjectMapper om = new ObjectMapper();

    /** stop 응답의 fileList JSON을 파싱해 file_objects 저장 → meetings.recording_file 연결 → CLOVA 전사 제출 */
    @Transactional
    public void handleStopAndSubmit(Long meetingId, String fileListJson) {
        try {
            JsonNode arr = (fileListJson != null && !fileListJson.isBlank())
                    ? om.readTree(fileListJson) : om.createArrayNode();
            if (!arr.isArray() || arr.size() == 0) return;

            String storageKey = arr.get(0).get("fileName").asText(); // recordings/event-xxx/xxxx.mp4|m3u8
            String fileName = storageKey.substring(storageKey.lastIndexOf('/') + 1);

            FileObject f = new FileObject();
            f.setStorageKey(storageKey);
            f.setFileName(fileName);
            f.setMimeType(fileName.endsWith(".mp4") ? "audio/mp4" : "application/vnd.apple.mpegurl");

            Meeting m = meetingRepo.findById(meetingId).orElseThrow();
            if (m.getOrganization() != null) f.setOrganization(m.getOrganization()); // 선택: 조직 연동
            fileRepo.save(f);

            m.setRecordingFile(f);
            meetingRepo.save(m);

            String base = props.getPublicBaseUrl().replaceAll("/+$", "");
            String audioUrl = base + "/" + storageKey;
            clova.submitForMeeting(meetingId, audioUrl, true);
        } catch (Exception e) {
            throw new RuntimeException("Finalize recording failed", e);
        }
    }
}
