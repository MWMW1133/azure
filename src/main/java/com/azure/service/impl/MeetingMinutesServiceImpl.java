package com.azure.service.impl;

import com.azure.model.meeting.Meeting;
import com.azure.model.meeting.MeetingSummary;
import com.azure.model.meeting.MeetingTranscript;
import com.azure.repository.MeetingRepository;
import com.azure.repository.MeetingSummaryRepository;
import com.azure.repository.MeetingTranscriptRepository;
import com.azure.service.MeetingMinutesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingMinutesServiceImpl implements MeetingMinutesService {

    private final MeetingRepository meetingRepo;
    private final MeetingTranscriptRepository transcriptRepo;
    private final MeetingSummaryRepository summaryRepo;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Override
    public MeetingSummary summarizeAndSave(Long meetingId) {
        // 1) 회의 존재 확인
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("회의가 존재하지 않습니다: id=" + meetingId));

        // 2) 전사 수집 (ASC 정렬 우선 사용, 없으면 정렬)
        List<MeetingTranscript> parts = transcriptRepo.findByMeeting_IdOrderByIdAsc(meetingId);
        if (parts == null || parts.isEmpty()) {
            parts = transcriptRepo.findByMeeting_Id(meetingId)
                    .stream().sorted(Comparator.comparingLong(MeetingTranscript::getId)).toList();
        }
        if (parts.isEmpty()) {
            throw new IllegalStateException("전사 데이터가 없습니다: meetingId=" + meetingId);
        }

        String lang = parts.get(0).getLang() != null ? parts.get(0).getLang() : "ko-KR";
        String full = parts.stream()
                .map(MeetingTranscript::getContent)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining("\n"));

        // 3) 청크 요약 → 병합 요약
        List<String> chunks = chunkByLength(full, 3200);   // 한글 기준 3.2k자 정도
        List<String> chunkSummaries = new ArrayList<>();
        for (String c : chunks) {
            chunkSummaries.add(callOpenAi(makeChunkPrompt(lang, c)));
        }
        String merged = String.join("\n\n", chunkSummaries);
        String summaryMd = callOpenAi(makeFinalPrompt(lang, merged));

        // 4) 업서트 저장
        MeetingSummary entity = summaryRepo.findTopByMeeting_IdOrderByIdDesc(meetingId).orElse(null);
        if (entity == null) entity = new MeetingSummary();
        entity.setMeeting(meeting);
        entity.setSummaryMd(summaryMd);
        // actionItems는 후처리 시 추출 가능
        return summaryRepo.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingSummary getLatest(Long meetingId) {
        return summaryRepo.findTopByMeeting_IdOrderByIdDesc(meetingId).orElse(null);
    }

    // ----- Prompt -----
    private String makeChunkPrompt(String lang, String chunk) {
        return """
            당신은 한국어 비즈니스 회의록 보조자입니다.
            아래는 음성 회의 전사의 일부입니다. 반복/잡음을 제외하고 핵심을 5줄 이내 불릿으로 요약하세요.
            수치/기한/담당자를 최대한 보존하세요.
            언어: %s

            [전사 일부]
            %s
            """.formatted(lang, chunk);
    }

    private String makeFinalPrompt(String lang, String merged) {
        return """
            당신은 한국어 비즈니스 회의록 보조자입니다.
            다음은 전사 청크 요약들의 묶음입니다. 이를 바탕으로 최종 회의록을 만드세요.
            출력은 반드시 아래 3개 Markdown 섹션만 포함해야 합니다.

            # 회의 주제
            - (한 줄 제목)

            # 회의 내용
            - 의제별 핵심 논점/결정/근거 (불릿)

            # 결론
            - 최종 합의/보류/추후 확인
            - 다음 액션 3~5개: (담당자/기한/산출물)

            언어: %s

            [요약 묶음]
            %s
            """.formatted(lang, merged);
    }

    // ----- OpenAI -----
    private String callOpenAi(String prompt) {
        try {
            String body = """
            {
              "model": "gpt-4o-mini",
              "temperature": 0.2,
              "messages": [
                {"role":"system","content":"You are a helpful assistant that writes concise Korean business meeting minutes."},
                {"role":"user","content": %s}
              ]
            }
            """.formatted(json(prompt));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
            String json = res.body();

            // 최소 파싱 (프로덕션에서는 Jackson 사용 권장)
            int i = json.indexOf("\"content\":");
            if (i < 0) return "";
            int s = json.indexOf("\"", i + 10) + 1;
            int e = json.indexOf("\"", s);
            return json.substring(s, e).replace("\\n", "\n");
        } catch (Exception e) {
            throw new RuntimeException("OpenAI 호출 실패", e);
        }
    }

    // ----- Utils -----
    private static List<String> chunkByLength(String s, int max) {
        List<String> out = new ArrayList<>();
        if (s == null) return out;
        for (int i = 0; i < s.length(); i += max) out.add(s.substring(i, Math.min(s.length(), i + max)));
        return out;
    }
    private static String json(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
