package com.azure.config;

import com.azure.websocket.PresenceInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * STOMP WebSocket 설정
 * - /ws 엔드포인트(SockJS 허용)
 * - /topic 브로커 사용 (서버가 publish하면 클라이언트가 구독)
 * - 클라이언트 인바운드 채널에 PresenceInterceptor 장착(구독/해제 탐지)
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final PresenceInterceptor presenceInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 개발 편의. 운영은 도메인 제한
                .withSockJS();                 // SockJS fallback 허용
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");  // 구독 prefix
        registry.setApplicationDestinationPrefixes("/app"); // (필요 시) 서버 수신 prefix
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 구독/해제/연결 이벤트를 가로채 프레즌스 처리
        registration.interceptors(presenceInterceptor);
    }
}
