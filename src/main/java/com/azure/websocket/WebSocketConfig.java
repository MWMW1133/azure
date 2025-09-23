package com.azure.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 최초 연결하는 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // 개발 단계: 모두 허용
        // .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 서버 → 클라이언트 송신 prefix(구독 경로)
        registry.enableSimpleBroker("/topic", "/queue");
        // 클라이언트 → 서버 송신 prefix(메시지 보낼 때)
        registry.setApplicationDestinationPrefixes("/app");
        // 사용자 개별 큐(선택)
        registry.setUserDestinationPrefix("/user");
    }
}
