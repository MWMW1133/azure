package com.azure.service.impl;

import com.azure.model.file.FileObject;
import com.azure.model.meeting.Meeting;
import com.azure.model.meeting.MeetingTranscript;
import com.azure.repository.FileObjectRepository;
import com.azure.repository.MeetingRepository;
import com.azure.repository.MeetingTranscriptRepository;
import com.azure.service.TranscriptService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 전사 저장 구현.
 * - 로컬 디스크에 .txt 파일 생성 → file_objects INSERT
 * - 같은 내용 그대로 meeting_transcripts에도 INSERT(검색/요약용)
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TranscriptServiceImpl implements TranscriptService {

    private final MeetingRepository meetingRepository;
    private final MeetingTranscriptRepository meetingTranscriptRepository;
    private final FileObjectRepository fileObjectRepository;

    /** 파일 저장 루트 (application.properties의 storage.base-dir) */
    @Value("${storage.base-dir:/var/azure/uploads}")
    private String baseDir;

    @Override
    public FileObject saveFinalTranscript(Long meetingId, Long uploaderId, String lang, String content) {
        // 1) 회의 존재 확인
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("Meeting not found: " + meetingId));

        // 2) 파일 경로/이름 구성: /{base}/meetings/{id}/transcript-YYYYMMDD-HHmmss.txt
        String ts   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String name = "transcript-" + ts + ".txt";
        Path dir    = Path.of(baseDir, "meetings", String.valueOf(meetingId));
        Path path   = dir.resolve(name);

        try {
            // 디렉터리 생성 후 파일 쓰기(UTF-8)
            Files.createDirectories(dir);
            byte[] bytes = (content == null ? "" : content).getBytes(StandardCharsets.UTF_8);
            Files.write(path, bytes);

            // 3) file_objects 메타 저장
            FileObject fo = new FileObject();
            fo.setStorageKey(path.toString()); // 운영에선 S3 키 등으로 교체
            fo.setFileName(name);
            fo.setMimeType("text/plain");
            fo.setSize((long) bytes.length);
            if (uploaderId != null) {
                var u = new com.azure.model.user.User();
                u.setId(uploaderId);
                fo.setUploader(u); // FK 세팅
            }
            fo = fileObjectRepository.save(fo);

            // 4) meeting_transcripts 저장(본문도 DB에 남겨 검색/요약 등에 활용)
            MeetingTranscript t = new MeetingTranscript();
            t.setMeeting(meeting);
            t.setLang(lang != null ? lang : "ko-KR");
            t.setContent(content);
            meetingTranscriptRepository.save(t);

            return fo;
        } catch (Exception e) {
            // 저장 실패 시 런타임 예외로 감싸 상위(컨트롤러)에서 500 처리
            throw new RuntimeException("Transcript save failed", e);
        }
    }
    // 회의록 본문 조회(없으면 빈 문자열)
    @Override
    @Transactional(readOnly = true)
    public String getTranscriptContent(Long meetingId) {
        return meetingTranscriptRepository.findByMeetingId(meetingId)
                .map(MeetingTranscript::getContent)
                .orElse("");
    }

}
