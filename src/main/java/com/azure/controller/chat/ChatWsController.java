package com.azure.controller.chat;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.azure.dto.MessageDTO;
import com.azure.model.chat.Message;
import com.azure.repository.ChatChannelRepository;
import com.azure.repository.MessageRepository;
import com.azure.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepo;
    private final ChatChannelRepository channelRepo;
    private final UserRepository userRepo;

    @Transactional
    @MessageMapping("/chat/{channelId}/send")
    public void send(@DestinationVariable Long channelId, @Payload MessageDTO payload) {
        log.info("[WS] recv -> ch={}, author={}, body={}", channelId, payload.getAuthorId(), payload.getBody());

        // 브로드캐스트용 DTO 초안
        MessageDTO out = new MessageDTO();
        out.setChannelId(channelId);
        out.setAuthorId(payload.getAuthorId());
        out.setBody(payload.getBody());
        out.setCreatedAt(LocalDateTime.now());

        try {
            // FK가 유효해야 함 (없으면 여기서 예외)
            var channelRef = channelRepo.getReferenceById(channelId);

            Message m = new Message();
            m.setChannel(channelRef);

            if (payload.getAuthorId() != null) {
                m.setAuthor(userRepo.getReferenceById(payload.getAuthorId()));
            }
            m.setBody(payload.getBody());
            m.setCreatedAt(LocalDateTime.now());

            var saved = messageRepo.save(m);
            // 저장 성공 시 실제 값 반영
            out.setId(saved.getId());
            out.setCreatedAt(saved.getCreatedAt());
        } catch (DataIntegrityViolationException e) {
            // FK 실패 등 저장 문제 → 로그만 남기고 계속 브로드캐스트
            log.warn("[WS] save failed (FK or constraint). Broadcasting anyway. ch={}, author={}",
                    channelId, payload.getAuthorId(), e);
        } catch (Exception e) {
            log.error("[WS] unexpected error while saving", e);
        }

        var topic = "/topic/chat/" + channelId;
        messagingTemplate.convertAndSend(topic, out);
        log.info("[WS] send -> {} : {}", topic, out.getBody());
    }
}
