package com.azure.service.impl;

import com.azure.config.S3Props;
import com.azure.config.TranscribeProps;
import com.azure.model.enums.MeetingStatus;
import com.azure.model.file.FileObject;
import com.azure.model.meeting.Meeting;
import com.azure.model.meeting.MeetingTranscript;
import com.azure.repository.FileObjectRepository;
import com.azure.repository.MeetingRepository;
import com.azure.repository.MeetingTranscriptRepository;
import com.azure.service.TranscriptService;
import com.azure.service.s3.S3StorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.LanguageCode;
import software.amazon.awssdk.services.transcribe.model.Media;
import software.amazon.awssdk.services.transcribe.model.MediaFormat;
import software.amazon.awssdk.services.transcribe.model.StartTranscriptionJobRequest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TranscriptServiceImpl implements TranscriptService {

    private final MeetingRepository meetingRepository;
    private final MeetingTranscriptRepository meetingTranscriptRepository;
    private final FileObjectRepository fileObjectRepository;
    private final S3StorageService s3;

    private final TranscribeClient transcribeClient;
    private final S3Props s3Props;
    private final TranscribeProps transcribeProps;

    @Value("${storage.base-dir:/var/azure/uploads}")
    private String baseDir;

    @Override
    public void handleSubmit(Long meetingId, String audioUrl, String mediaType, String lang) {
        log.info("handleSubmit for meetingId: {}, audioUrl: {}", meetingId, audioUrl);

        // 1) 회의 조회
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found with id: " + meetingId));

        // 2) S3 공개 URL -> 키/파일명
        String s3Key = s3.deriveKeyFromPublicUrl(audioUrl);          // org/.../1760576842190.webm
        String fileName = s3Key.substring(s3Key.lastIndexOf('/') + 1);

        // ✅ 녹음 원본은 file_objects에 저장/연결하지 않음
        meeting.setStatus(MeetingStatus.TRANSCRIBING);
        meetingRepository.save(meeting);

        // 5) Transcribe 잡 시작
        String mediaUri = "s3://" + s3Props.getBucket() + "/" + s3Key;
        String format = guessFormat(mediaType, fileName); // webm/wav/mp3/mp4...
        MediaFormat mediaFormat = MediaFormat.fromValue(format);
        String langCode = (lang == null || lang.isBlank()) ? "ko-KR" : lang;
        LanguageCode languageCode = LanguageCode.fromValue(langCode);

        String jobName = buildJobName(meetingId, s3Key);  // meeting-65-1760576842190

        log.info("StartTranscription: job={}, uri={}, format={}, lang={}, outBucket={}",
                jobName, mediaUri, format, langCode, transcribeProps.getResultsBucket());

        StartTranscriptionJobRequest req = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .languageCode(languageCode)
                .media(Media.builder().mediaFileUri(mediaUri).build())
                .mediaFormat(mediaFormat)
                .outputBucketName(transcribeProps.getResultsBucket())
                .build();

        transcribeClient.startTranscriptionJob(req);
    }

    // ===== 기존 최종본 저장/조회 유지 =====

    @Override
    public FileObject saveFinalTranscript(Long meetingId, Long uploaderId, String lang, String content) {
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String name = "transcript-" + ts + ".txt";
        Path dir = Path.of(baseDir, "meetings", String.valueOf(meetingId));
        Path path = dir.resolve(name);
        try {
            Files.createDirectories(dir);
            byte[] bytes = (content == null ? "" : content).getBytes(StandardCharsets.UTF_8);
            Files.write(path, bytes);

            FileObject fo = new FileObject();
            fo.setStorageKey(path.toString());
            fo.setFileName(name);
            fo.setMimeType("text/plain");
            fo.setSize((long) bytes.length);
            fo.setOrganization(meeting.getOrganization());
            if (uploaderId != null) {
                var u = new com.azure.model.user.User();
                u.setId(uploaderId);
                fo.setUploader(u);
            }
            fo = fileObjectRepository.save(fo);

            MeetingTranscript t = new MeetingTranscript();
            t.setMeeting(meeting);
            t.setLang((lang != null && !lang.isBlank()) ? lang : "ko-KR");
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

    // ===== 내부 유틸 =====
    private static String buildJobName(Long meetingId, String inputKey) {
        String file = inputKey.substring(inputKey.lastIndexOf('/') + 1); // 1760576842190.webm
        String ts   = file.replaceFirst("\\..+$", "");                   // 1760576842190
        return "meeting-" + meetingId + "-" + ts;
    }

    private static String guessFormat(String mediaType, String fileName) {
        if (mediaType != null) {
            String mt = mediaType.toLowerCase();
            if (mt.contains("webm")) return "webm";
            if (mt.contains("wav"))  return "wav";
            if (mt.contains("mp4"))  return "mp4";
            if (mt.contains("mp3"))  return "mp3";
            if (mt.contains("flac")) return "flac";
            if (mt.contains("amr"))  return "amr";
        }
        String ext = fileName.replaceAll("^.*\\.(\\w+)$", "$1").toLowerCase();
        return switch (ext) {
            case "webm" -> "webm";
            case "wav"  -> "wav";
            case "mp4"  -> "mp4";
            case "mp3"  -> "mp3";
            case "flac" -> "flac";
            case "amr"  -> "amr";
            default     -> "webm";
        };
    }
}