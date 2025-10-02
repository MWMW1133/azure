package com.azure.websocket;

import com.azure.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * STOMP 구독/연결/해제 신호를 가로채서
 * - /topic/meetings/{id}/presence 구독 시 → 프레즌스 join
 * - DISCONNECT 시 → 프레즌스 leave
 * 그리고 현재 접속자 리스트를 해당 토픽으로 브로드캐스트.
 */
@Component
@RequiredArgsConstructor
public class PresenceInterceptor implements ChannelInterceptor {

    private final PresenceService presence;
    private final SimpMessagingTemplate broker;
    private final Pattern presenceDest = Pattern.compile("/topic/meetings/(\\d+)/presence");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var acc = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
            var m = presenceDest.matcher(acc.getDestination());
            if (m.find()) {
                Long meetingId = Long.valueOf(m.group(1));
                String sessionId = acc.getSessionId();
                // 클라이언트가 구독 헤더로 전달한 표시 이름
                String name = acc.getFirstNativeHeader("X-User-Name");
                if (name == null || name.isBlank()) name = "익명";

                presence.join(meetingId, sessionId, name);
                // 접속자 전체 리스트를 즉시 브로드캐스트 → 우측 패널 실시간 갱신
                broker.convertAndSend("/topic/meetings/" + meetingId + "/presence",
                        presence.list(meetingId));
            }
        } else if (StompCommand.DISCONNECT.equals(acc.getCommand())) {
            // 어떤 meetingId인지 알 수 없으므로 전체에서 세션 제거
            presence.leave(acc.getSessionId());
            // (선택) 클라이언트가 /app/meetings/{id}/leave 를 쏘도록 설계해
            // 정확한 회의 방만 재방송해도 됨.
        }
        return message;
    }
}
