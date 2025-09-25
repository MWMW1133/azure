package com.azure.service;

import com.azure.model.chat.ChatChannel;
import com.azure.model.chat.Message;
import com.azure.model.enums.ChannelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 채팅 채널/메시지/읽음 표시를 관리한다.
 */
public interface ChatService {
    /** 채널 생성(프로젝트/그룹/DM 등). */
    ChatChannel createChannel(ChannelType type, Long projectId, String name, Long createdBy);

    /** 채널 멤버 추가/삭제. */
    void addMember(Long channelId, Long userId);
    void removeMember(Long channelId, Long userId);

    /** 메시지 전송(파일/답글 옵션 포함). */
    Message postMessage(Long channelId, Long authorId, String body, Long fileId, Long replyToId);

    /** 메시지 목록(페이징). */
    Page<Message> listMessages(Long channelId, Pageable pageable);

    /** 읽음 위치 업데이트(마지막 읽은 메시지 ID 저장). */
    void markRead(Long channelId, Long userId, Long lastReadMessageId);
}
