package com.azure.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

      @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ 알림(레거시) 호환
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();

        // ✅ 채팅 전용
        registry.addEndpoint("/ws-chat")
            .setAllowedOriginPatterns("*")
            .withSockJS();
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
