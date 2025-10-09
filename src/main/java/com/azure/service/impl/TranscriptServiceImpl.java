package com.azure.service.impl;

import com.azure.model.file.FileObject;
import com.azure.model.meeting.Meeting;
import com.azure.model.meeting.MeetingTranscript;
import com.azure.repository.FileObjectRepository;
import com.azure.repository.MeetingRepository;
import com.azure.repository.MeetingTranscriptRepository;
import com.azure.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Transactional
@RequiredArgsConstructor
public class TranscriptServiceImpl implements TranscriptService {

    private final MeetingRepository meetingRepository;
    private final MeetingTranscriptRepository meetingTranscriptRepository;
    private final FileObjectRepository fileObjectRepository;

    @Value("${storage.base-dir:/var/azure/uploads}")
    private String baseDir;

    @Override
    public FileObject saveFinalTranscript(Long meetingId, Long uploaderId, String lang, String content) {
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();

        String ts   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String name = "transcript-" + ts + ".txt";
        Path dir    = Path.of(baseDir, "meetings", String.valueOf(meetingId));
        Path path   = dir.resolve(name);

        try {
            Files.createDirectories(dir);
            byte[] bytes = (content == null ? "" : content).getBytes(StandardCharsets.UTF_8);
            Files.write(path, bytes);

            FileObject fo = new FileObject();
            fo.setStorageKey(path.toString());
            fo.setFileName(name);
            fo.setMimeType("text/plain");
            fo.setSize((long) bytes.length);
            fo.setOrganization(meeting.getOrganization()); // NOT NULL
            if (uploaderId != null) {
                var u = new com.azure.model.user.User();
                u.setId(uploaderId);
                fo.setUploader(u);
            }
            fo = fileObjectRepository.save(fo);

            MeetingTranscript t = new MeetingTranscript();
            t.setMeeting(meeting);
            t.setLang(lang != null ? lang : "ko-KR");
            t.setContent(content);
            meetingTranscriptRepository.save(t);

            return fo;
        } catch (Exception e) {
            throw new RuntimeException("Transcript save failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getTranscriptContent(Long meetingId) {
        return meetingTranscriptRepository.findTopByMeeting_IdOrderByIdDesc(meetingId)
                .map(MeetingTranscript::getContent)
                .orElse("");
    }
}
