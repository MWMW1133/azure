// src/main/java/com/azure/service/ChatMessageEventBridge.java
package com.azure.service.chat;

import com.azure.event.ChatMessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor

/**
 * 채팅 메시지 저장 후 발행되는 {@link com.azure.event.ChatMessageCreatedEvent}
 * 를 수신해서, STOMP 브로커(/topic)를 통해 구독 중인 클라이언트로
 * 실시간 푸시하는 브리지 컴포넌트.
 *
 * <p><b>동작</b>
 * - @EventListener 로 이벤트 수신
 * - 최소 페이로드(id, channelId, senderId, preview)만 만들어 전송
 *   (예: "/topic/chat/{channelId}")
 * - 상세 데이터가 필요하면 클라이언트가 REST 재조회
 *
 * <p><b>왜 최소 페이로드인가?</b>
 * - 전송량/메모리 절약, 브로커 부하 감소, 보안적으로도 안전
 *
 * <p><b>전제 조건</b>
 * - {@code WebSocketConfig} 에서 STOMP 엔드포인트("/ws-chat")와
 *   브로커 prefix("/topic")가 활성화되어 있어야 함
 *
 * <p><b>트랜잭션</b>
 * - DB 접근 없음. 이벤트 수신 → 브로커 전송만 수행
 */

public class ChatMessageEventBridge {

    private final SimpMessagingTemplate template;

    @EventListener
    /** 저장 완료된 메시지 이벤트를 수신하여 STOMP 토픽(/topic/chat/{channelId})으로 브로드캐스트. */
    public void onCreated(ChatMessageCreatedEvent e) {
        var payload = Map.of(
                "id",        e.messageId(),
                "channelId", e.channelId(),
                "senderId",  e.authorId(),
                "preview",   e.preview()
        );
        template.convertAndSend("/topic/chat/" + e.channelId(), payload);
    }
}
