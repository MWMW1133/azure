package com.azure.config;

import com.azure.repository.ChatChannelRepository;
import com.azure.repository.UserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.security.Principal;

@Component
public class ChannelAuthInterceptor implements ChannelInterceptor {

    private final ChatChannelRepository channelRepo;
    private final UserRepository userRepo;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // ✅ 채팅 경로 패턴 (구독/전송 모두 지원)
    private static final String TOPIC_PATTERN    = "/topic/chat/{channelId}";     // SUBSCRIBE
    private static final String APP_SEND_PATTERN = "/app/chat/{channelId}/send";  // SEND

    public ChannelAuthInterceptor(ChatChannelRepository channelRepo, UserRepository userRepo) {
        this.channelRepo = channelRepo;
        this.userRepo = userRepo;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var acc = StompHeaderAccessor.wrap(message);
        var cmd = acc.getCommand();

        // 1) 연결 단계/하트비트는 무조건 통과 (연결 자체는 막지 않음)
        if (cmd == null
                || cmd == StompCommand.CONNECT
                || cmd == StompCommand.DISCONNECT
                || acc.getMessageType() == SimpMessageType.HEARTBEAT) {
            return message;
        }

        // 2) 구독/전송만 검사
        if (cmd != StompCommand.SUBSCRIBE && cmd != StompCommand.SEND) {
            return message;
        }

        // 3) 로그인 검사
        Principal principal = acc.getUser();
        if (principal == null) return null;

        var me = userRepo.findByLoginId(principal.getName()).orElse(null);
        if (me == null) return null;

        // 4) 목적지에서 channelId 추출
        String dest = acc.getDestination();
        Long channelId = extractChannelId(dest);

        // 채팅 목적지가 아니면 검사하지 않고 통과 (다른 WS 기능에 영향 X)
        if (channelId == null) return message;

        // 5) 멤버십 검사: 내가 그 채널 멤버인가?
        var myChannels = channelRepo.findByMembers_User_Id(me.getId());
        boolean isMember = myChannels.stream().anyMatch(ch -> ch.getId().equals(channelId));
        if (!isMember) return null; // 멤버 아니면 차단

        return message;
    }

    private Long extractChannelId(String dest) {
        if (dest == null) return null;
        try {
            // /topic/chat/{channelId}
            if (pathMatcher.match(TOPIC_PATTERN, dest)) {
                var vars = pathMatcher.extractUriTemplateVariables(TOPIC_PATTERN, dest);
                return Long.valueOf(vars.get("channelId"));
            }
            // /app/chat/{channelId}/send
            if (pathMatcher.match(APP_SEND_PATTERN, dest)) {
                var vars = pathMatcher.extractUriTemplateVariables(APP_SEND_PATTERN, dest);
                return Long.valueOf(vars.get("channelId"));
            }
        } catch (Exception ignore) { }
        return null;
    }
}
