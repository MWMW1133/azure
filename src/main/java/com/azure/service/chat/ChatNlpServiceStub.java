package com.azure.service.chat;

import org.springframework.stereotype.Service;

@Service

/**
 * 개발/데모용 더미 NLP 구현체.
 *
 * <p><b>동작</b>
 * - translate: 입력 텍스트를 그대로 반환(실제 번역 없음)
 * - summarize: 지정한 글자수로 단순 잘라내기
 *
 * <p><b>용도</b>
 * - UI 연동/플로우 검증, 네트워크 불가 환경에서의 테스트
 * - 운영에는 사용하지 말 것. 실제 API 구현체로 교체 권장
 *
 * <p><b>교체 방법 예시</b>
 * - @Profile("demo") 로 이 빈을, @Profile("prod") 로 실제 구현을 등록
 * - 또는 @Primary 우선순위를 변경해 런타임 선택
 */

public class ChatNlpServiceStub implements ChatNlpService {
    @Override
    public String translate(String text, String target) {
        return text; // TODO: 실제 번역 API로 교체
    }
    @Override
    public String summarize(String text, int maxChars) {
        if (text == null) return "";
        return text.length() <= maxChars ? text : text.substring(0, Math.max(0, maxChars)) + "...";
    }
}
