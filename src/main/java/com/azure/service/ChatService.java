package com.azure.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.azure.dto.ProjectChatDTO;
import com.azure.model.chat.ChatChannel;
import com.azure.model.chat.Message;
import com.azure.model.enums.ChannelType;

/**
 * 채팅 도메인 서비스
 */
public interface ChatService {

    ChatChannel createChannel(ChannelType type, Long projectId, String name, Long createdBy);

    void addMember(Long channelId, Long userId);
    void removeMember(Long channelId, Long userId);

    default Message postMessage(Long channelId, Long authorId, String body, Long fileId, Long replyToId) {
        return postMessage(channelId, authorId, body, fileId, replyToId, Boolean.FALSE, "en");
    }

    Message postMessage(Long channelId,
                        Long authorId,
                        String body,
                        Long fileId,
                        Long replyToId,
                        Boolean translateEnabled,
                        String targetLang);

    Page<Message> listMessages(Long channelId, Pageable pageable);

    void markRead(Long channelId, Long userId, Long lastReadMessageId);

    Long getOrCreateDmChannel(long me, long peer);

    /** 🔥 로그인 사용자 기준 프로젝트 채팅 목록 */
    List<ProjectChatDTO> listProjectRooms(Long userId);

    /** 프로젝트 기준 채널 get-or-create */
    Long getOrCreateProjectChannel(Long projectId, String projectName, Long createdBy);
}
