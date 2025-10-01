package com.azure.service.chat;

/**
 * 채팅 메시지의 "번역" / "요약" 기능을 추상화한 파사드 인터페이스.
 * 구현체를 갈아끼워 외부 API(파파고/클로바/OpenAI 등)로 연결하기 쉽도록 분리.
 *
 * <p><b>계약</b>
 * - {@code translate(text, targetLangCode)}: 대상 언어코드("ko","en","ja"...)
 * - {@code summarize(text, maxChars)}: 최대 글자수 범위의 짧은 요약 반환
 *
 * <p><b>특성</b>
 * - 순수 함수 성격(입력 텍스트 → 결과 텍스트), 부작용/상태 없음이 바람직
 * - 호출 실패 시 런타임 예외 또는 커스텀 예외로 통일
 * - (실서비스) 호출 제한/과금 고려: 캐싱/백오프/큐잉은 상위 레이어에서
 */

public interface ChatNlpService {
    String translate(String text, String targetLangCode); // "ko","en","ja",...
    String summarize(String text, int maxChars);          // 간단 요약
}
