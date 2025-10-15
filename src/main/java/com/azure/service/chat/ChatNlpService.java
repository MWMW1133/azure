package com.azure.service.chat;

/**
 * 채팅 메시지의 "번역" / "요약" 기능을 추상화한 파사드 인터페이스.
 * 구현체를 갈아끼워 외부 API(파파고/클로바/DeepL/MS Translator 등)로 연결하기 쉽도록 분리.
 *
 * 계약
 * - translate(text, targetLangCode): 대상 언어코드("ko","en","ja","zh-CN" 등). source는 구현체에서 auto-detect.
 * - summarize(text, maxChars): 최대 글자수 범위의 짧은 요약 반환
 *
 * 특성
 * - 순수 함수 성격(입력 텍스트 → 결과 텍스트), 부작용 없음이 바람직
 * - 호출 실패 시: 구현체에서 원문을 그대로 돌려주거나 런타임 예외 통일
 */
public interface ChatNlpService {
    String translate(String text, String targetLang); // targetLang: "ko" | "en" | "ja" | "zh"
    String summarize(String text);
}
