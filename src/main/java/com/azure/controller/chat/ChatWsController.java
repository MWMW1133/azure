package com.azure.controller.chat;

import com.azure.dto.MessageDTO;
import com.azure.model.chat.Message;
import com.azure.service.ChatService;                    // ★ 서비스로 위임
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;               // ★ 레포지토리 대신 서비스 주입

    /**
     * 클라이언트 publish: /app/chat/{channelId}/send
     * body(JSON): {
     *   "authorId":1, "body":"안녕",
     *   "translateEnabled":true, "targetLang":"en|ko|ja|zh",
     *   "fileId":null, "replyToId":null
     * }
     */
    @Transactional
    @MessageMapping("/chat/{channelId}/send")
    public void send(@DestinationVariable Long channelId, @Payload SendPayload payload) {
        log.info("[WS] recv -> ch={}, author={}, tr?={}, tgt={}, body={}",
                channelId, payload.authorId(), payload.translateEnabled(), payload.targetLang(), payload.body());

        // ★ 서비스에 '번역 옵션' 포함하여 저장 (번역은 서비스/스텁에서 처리됨)
        Message saved = chatService.postMessage(
                channelId,
                payload.authorId(),
                payload.body(),
                payload.fileId(),
                payload.replyToId(),
                payload.translateEnabled(),
                payload.targetLang()
        );

        // 브로드캐스트용 DTO
        MessageDTO out = new MessageDTO();
        out.setId(saved.getId());
        out.setChannelId(channelId);
        out.setAuthorId(saved.getAuthor() != null ? saved.getAuthor().getId() : payload.authorId());
        out.setBody(saved.getBody()); // ★ 번역이 적용된 본문
        out.setCreatedAt(saved.getCreatedAt() != null ? saved.getCreatedAt() : LocalDateTime.now());

        String topic = "/topic/chat/" + channelId;
        messagingTemplate.convertAndSend(topic, out);
        log.info("[WS] send -> {} : {}", topic, out.getBody());
    }

    // ===== 수신 페이로드 DTO =====
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record SendPayload(
            Long authorId,
            String body,
            Boolean translateEnabled,
            String targetLang,
            Long fileId,
            Long replyToId
    ) {}
}
