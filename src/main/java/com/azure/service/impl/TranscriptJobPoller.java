package com.azure.service.impl;

import com.azure.config.TranscribeProps;
import com.azure.model.enums.MeetingStatus;
import com.azure.model.file.FileObject;
import com.azure.model.meeting.Meeting;
import com.azure.model.meeting.MeetingTranscript;
import com.azure.repository.MeetingRepository;
import com.azure.repository.MeetingTranscriptRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.BadRequestException;
import software.amazon.awssdk.services.transcribe.model.GetTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJob;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJobStatus;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class TranscriptJobPoller {

    private final MeetingRepository meetingRepository;
    private final MeetingTranscriptRepository meetingTranscriptRepository;

    private final TranscribeClient transcribeClient;
    private final S3Client s3Client;
    private final TranscribeProps transcribeProps;

    private final ObjectMapper om = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // "job not found" 연속 N회면 FAILED 처리 (기본: 120회 = 10분 @5초 간격)
    private static final int NOT_FOUND_MAX = Integer.getInteger("transcribe.poll.notfound.max", 120);

    // 회의ID 기준 not-found 카운터 (메모리)
    private final Map<Long, Integer> notFoundCounts = new ConcurrentHashMap<>();

    // 기본: 5초 딜레이, 최초 10초 대기 — application.properties 로 조절 가능
    // transcribe.poll.fixed-delay=5000
    // transcribe.poll.initial-delay=10000
    @Scheduled(
            fixedDelayString = "${transcribe.poll.fixed-delay:5000}",
            initialDelayString = "${transcribe.poll.initial-delay:10000}"
    )
    @Transactional
    public void poll() {
        List<Meeting> targets = meetingRepository.findByStatusAndRecordingFileIsNotNull(MeetingStatus.TRANSCRIBING);
        if (targets.isEmpty()) return;

        for (Meeting m : targets) {
            FileObject rec = m.getRecordingFile();
            if (rec == null || rec.getStorageKey() == null) continue;

            String jobName = buildJobName(m.getId(), rec.getStorageKey());
            try {
                TranscriptionJob job = transcribeClient.getTranscriptionJob(
                        GetTranscriptionJobRequest.builder()
                                .transcriptionJobName(jobName).build()
                ).transcriptionJob();

                TranscriptionJobStatus st = job.transcriptionJobStatus();
                switch (st) {
                    case IN_PROGRESS, QUEUED -> {
                        // 진행 중: 아무 것도 하지 않음
                    }
                    case COMPLETED -> {
                        String transcriptText = fetchTranscriptText(jobName, job.transcript().transcriptFileUri());
                        persistResult(m, transcriptText, detectLangFromJobName(jobName));
                        resetCounters(m.getId());
                        log.info("Transcribe COMPLETED: meeting={}, job={}", m.getId(), jobName);
                    }
                    case FAILED -> {
                        m.setStatus(MeetingStatus.FAILED);
                        meetingRepository.save(m);
                        resetCounters(m.getId());
                        log.warn("Transcribe FAILED: meeting={}, job={}", m.getId(), jobName);
                    }
                    default -> { /* no-op */ }
                }
            } catch (BadRequestException e) {
                // 이름/레이스 문제로 job을 못 찾을 때: 일정 횟수까지는 대기, 초과 시 FAILED
                int c = notFoundCounts.merge(m.getId(), 1, Integer::sum);
                if (c == 1 || c % 12 == 0) { // 1회 및 매 분마다만 경고 로그
                    log.warn("Transcribe job not found yet ({} / {}): meeting={}, job={}",
                            c, NOT_FOUND_MAX, m.getId(), jobName);
                }
                if (c >= NOT_FOUND_MAX) {
                    m.setStatus(MeetingStatus.FAILED);
                    meetingRepository.save(m);
                    resetCounters(m.getId());
                    log.error("Marking as FAILED due to repeated 'job not found': meeting={}, job={}", m.getId(), jobName);
                }
            } catch (Exception e) {
                // 기타 오류: 로그만, 상태 보존(다음 틱에 재시도)
                log.error("Unexpected error while polling meeting={}, job={}: {}", m.getId(), jobName, e.toString());
            }
        }
    }

    /** 서비스의 규칙과 동일: "meeting-{meetingId}-{파일명(확장자제외)}" */
    private static String buildJobName(Long meetingId, String inputKey) {
        String file = inputKey.substring(inputKey.lastIndexOf('/') + 1);
        String ts = file.replaceFirst("\\..+$", "");
        return "meeting-" + meetingId + "-" + ts;
    }

    /** 결과 JSON에서 transcripts[0].transcript 추출 */
    private String parseTranscriptJson(byte[] jsonBytes) throws Exception {
        JsonNode root = om.readTree(jsonBytes);
        JsonNode transcripts = root.path("results").path("transcripts");
        if (transcripts.isArray() && transcripts.size() > 0) {
            JsonNode first = transcripts.get(0);
            if (first.hasNonNull("transcript")) {
                return first.get("transcript").asText("");
            }
        }
        return "";
    }

    /** 결과 버킷이 있으면 S3에서, 없으면 transcriptFileUri(HTTP)에서 가져옴 */
    private String fetchTranscriptText(String jobName, String transcriptFileUri) throws Exception {
        String bucket = transcribeProps.getResultsBucket();
        String prefix = transcribeProps.getOutputPrefix();
        String key = (prefix == null || prefix.isBlank())
                ? (jobName + ".json")
                : (prefix.replaceAll("/+$", "") + "/" + jobName + ".json");

        byte[] jsonBytes = null;

        if (bucket != null && !bucket.isBlank()) {
            try (InputStream in = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build())) {
                jsonBytes = readAll(in);
            } catch (NoSuchKeyException e) {
                log.warn("Transcript JSON not found in S3. bucket={}, key={}, fallback to URI", bucket, key);
            }
        }

        if (jsonBytes == null) {
            // outBucket=null 인 경우 등: AWS가 준 pre-signed URL 사용
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(transcriptFileUri))
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();
            HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() / 100 != 2) {
                throw new IllegalStateException("Failed to download transcript from URI: " + resp.statusCode());
            }
            jsonBytes = resp.body();
        }

        return parseTranscriptJson(jsonBytes);
    }

    private static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) != -1) {
            bos.write(buf, 0, r);
        }
        return bos.toByteArray();
    }

    private void persistResult(Meeting meeting, String content, String lang) {
        MeetingTranscript t = new MeetingTranscript();
        t.setMeeting(meeting);
        t.setLang((lang == null || lang.isBlank()) ? "ko-KR" : lang);
        t.setContent(content == null ? "" : content);
        meetingTranscriptRepository.save(t);

        meeting.setStatus(MeetingStatus.COMPLETED);
        meetingRepository.save(meeting);
    }

    // 간단한 언어 추정(파일명 규칙에 언어가 없다면 기본 ko-KR)
    private String detectLangFromJobName(String jobName) {
        // 필요 시 규칙 추가, 현재는 기본값
        return "ko-KR";
    }

    private void resetCounters(Long meetingId) {
        notFoundCounts.remove(meetingId);
    }
}
