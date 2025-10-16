package com.azure.service.chat;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API 키 없이 동작하는 간이 번역 스텁
 * - 최대 3줄까지만 번역
 * - 작성 언어(ko/ja/zh/en) 자동 감지 → targetLang(ko/en/ja/zh)로 변환
 * - 간단한 사전 치환 (pivot = EN)
 */
@Primary
/*@Service*/
public class ChatNlpServiceStub implements ChatNlpService {

    private static final int MAX_LINES = 3;
    private static final Set<String> ALLOWED = Set.of("ko", "en", "ja", "zh");

    // EN <-> 각 언어 사전
    private static final LinkedHashMap<String, String> EN_KO = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> EN_JA = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> EN_ZH = new LinkedHashMap<>();

    private static final LinkedHashMap<String, String> KO_EN = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> JA_EN = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> ZH_EN = new LinkedHashMap<>();

    static {
        // 긴 구문 -> 짧은 단어 순 (치환 안정성)
        EN_KO.put("thank you", "고마워");
        EN_KO.put("good morning", "좋은 아침");
        EN_KO.put("good night", "잘 자");
        EN_KO.put("see you", "또 보자");
        EN_KO.put("hello", "안녕");
        EN_KO.put("love", "사랑");
        EN_KO.put("friend", "친구");
        EN_KO.put("good", "좋아요");
        EN_KO.put("bad", "나빠요");
        EN_KO.put("yes", "응");
        EN_KO.put("no", "아니");
        EN_KO.put("today", "오늘");

        EN_JA.put("thank you", "ありがとう");
        EN_JA.put("good morning", "おはよう");
        EN_JA.put("good night", "おやすみ");
        EN_JA.put("see you", "またね");
        EN_JA.put("hello", "こんにちは");
        EN_JA.put("love", "愛");
        EN_JA.put("friend", "友達");
        EN_JA.put("good", "良い");
        EN_JA.put("bad", "悪い");
        EN_JA.put("yes", "はい");
        EN_JA.put("no", "いいえ");
        EN_JA.put("today", "今日");

        EN_ZH.put("thank you", "谢谢");
        EN_ZH.put("good morning", "早上好");
        EN_ZH.put("good night", "晚安");
        EN_ZH.put("see you", "再见");
        EN_ZH.put("hello", "你好");
        EN_ZH.put("love", "爱");
        EN_ZH.put("friend", "朋友");
        EN_ZH.put("good", "好");
        EN_ZH.put("bad", "坏");
        EN_ZH.put("yes", "是");
        EN_ZH.put("no", "不是");
        EN_ZH.put("today", "今天");

        invertTo(EN_KO, KO_EN);
        invertTo(EN_JA, JA_EN);
        invertTo(EN_ZH, ZH_EN);

        // 한국어 구어체 추가
        KO_EN.put("고마워요", "thank you");
        KO_EN.put("안녕하세요", "hello");
        KO_EN.put("잘자", "good night");
        KO_EN.put("또봐", "see you");
    }

    private static void invertTo(LinkedHashMap<String, String> src, LinkedHashMap<String, String> dst) {
        for (Map.Entry<String, String> e : src.entrySet()) {
            dst.put(e.getValue(), e.getKey());
        }
    }

    @Override
    public String translate(String text, String targetLang) {
        if (text == null || text.isBlank()) return "";

        // 3줄 제한
        String[] lines = text.split("\\r?\\n");
        if (lines.length > MAX_LINES) {
            return "[번역 생략: 3줄 초과]\n" + text;
        }

        // 타겟 언어 정규화
        String target = (targetLang == null) ? "en" : targetLang.toLowerCase(Locale.ROOT);
        if (!ALLOWED.contains(target)) target = "en";

        // 작성 언어 자동 감지
        String source = detectLanguage(text);
        if (source.equals(target)) {
            return text; // 이미 타겟이면 그대로
        }

        // 1) source -> EN
        String toEn = switch (source) {
            case "ko" -> replaceByDictionary(text, KO_EN, false);
            case "ja" -> replaceByDictionary(text, JA_EN, false);
            case "zh" -> replaceByDictionary(text, ZH_EN, false);
            default -> text; // 이미 EN
        };

        // 2) EN -> target
        return switch (target) {
            case "ko" -> replaceByDictionary(toEn, EN_KO, true);
            case "ja" -> replaceByDictionary(toEn, EN_JA, true);
            case "zh" -> replaceByDictionary(toEn, EN_ZH, true);
            default -> toEn;
        };
    }

    @Override
    public String summarize(String text) {
        if (text == null || text.isBlank()) return "";
        return "요약: " + (text.length() > 40 ? text.substring(0, 40) + "..." : text);
    }

    // ===== 유틸 =====

    /** 대략적 언어 감지: 한글/일본어/중국어 코드포인트 우선, 없으면 EN */
    private String detectLanguage(String s) {
        boolean hasKo = s.codePoints().anyMatch(cp ->
                (cp >= 0x1100 && cp <= 0x11FF) || (cp >= 0x3130 && cp <= 0x318F) || (cp >= 0xAC00 && cp <= 0xD7A3));
        if (hasKo) return "ko";

        boolean hasJa = s.codePoints().anyMatch(cp ->
                (cp >= 0x3040 && cp <= 0x309F) || (cp >= 0x30A0 && cp <= 0x30FF));
        if (hasJa) return "ja";

        boolean hasZh = s.codePoints().anyMatch(cp -> (cp >= 0x4E00 && cp <= 0x9FFF));
        if (hasZh) return "zh";

        return "en";
    }

    /**
     * 사전 치환
     * - 영어쪽은 단어경계(\\b) + 대소문자 무시
     * - CJK/한글은 literal 치환
     */
    private String replaceByDictionary(String text,
                                       LinkedHashMap<String, String> dict,
                                       boolean englishWordBoundary) {
        String out = text;
        for (Map.Entry<String, String> e : dict.entrySet()) {
            String src = e.getKey();
            String dst = e.getValue();
            if (englishWordBoundary) {
                out = replaceIgnoreCaseWord(out, src, dst);
            } else {
                out = out.replace(src, dst);
            }
        }
        return out;
    }

    private String replaceIgnoreCaseWord(String input, String find, String replace) {
        String pattern = "\\b" + Pattern.quote(find) + "\\b";
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(replace));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
