package com.azure.service.impl;

import com.azure.model.chat.*;
import com.azure.model.enums.ChannelType;
import com.azure.model.file.FileObject;
import com.azure.model.project.Project;
import com.azure.model.user.User;
import com.azure.repository.*;
import com.azure.service.ChatService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 채팅 서비스 구현.
 * - 메시지 읽음 표시(MessageRead)는 upsert 방식으로 저장
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatChannelRepository chatChannelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final FileObjectRepository fileObjectRepository;

    @Override
    public ChatChannel createChannel(ChannelType type, Long projectId, String name, Long createdBy) {
        ChatChannel c = new ChatChannel();
        c.setChannelType(type);
        if (projectId != null) { c.setProject(new Project()); c.getProject().setId(projectId); }
        c.setName(name);
        if (createdBy != null) { c.setCreatedBy(new User()); c.getCreatedBy().setId(createdBy); }
        return chatChannelRepository.save(c);
    }

    @Override
    public void addMember(Long channelId, Long userId) {
        ChatChannel channel = chatChannelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("Channel not found: " + channelId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        ChannelMember m = new ChannelMember();
        ChannelMemberId id = new ChannelMemberId();
        id.setChannelId(channel.getId()); id.setUserId(user.getId());
        m.setId(id); m.setChannel(channel); m.setUser(user);
        channelMemberRepository.save(m);
    }

    @Override
    public void removeMember(Long channelId, Long userId) {
        ChannelMemberId id = new ChannelMemberId();
        id.setChannelId(channelId); id.setUserId(userId);
        channelMemberRepository.deleteById(id);
    }

    @Override
    public Message postMessage(Long channelId, Long authorId, String body, Long fileId, Long replyToId) {
        ChatChannel channel = chatChannelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("Channel not found: " + channelId));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found: " + authorId));

        Message msg = new Message();
        msg.setChannel(channel);
        msg.setAuthor(author);
        msg.setBody(body);
        if (fileId != null) {
            FileObject f = fileObjectRepository.findById(fileId)
                    .orElseThrow(() -> new NotFoundException("File not found: " + fileId));
            msg.setFile(f);
        }
        if (replyToId != null) {
            Message ref = messageRepository.findById(replyToId)
                    .orElseThrow(() -> new NotFoundException("Message not found: " + replyToId));
            msg.setReplyTo(ref);
        }
        return messageRepository.save(msg);
    }

    @Override @Transactional(readOnly = true)
    public Page<Message> listMessages(Long channelId, Pageable pageable) {
        // 레포가 반환한 전체 목록을 임시 페이징
        List<Message> all = messageRepository.findByChannelIdOrderByIdAsc(channelId);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<Message> content = (start > end) ? List.of() : all.subList(start, end);
        return new PageImpl<>(content, pageable, all.size());
    }

    @Override
    public void markRead(Long channelId, Long userId, Long lastReadMessageId) {
        MessageReadId id = new MessageReadId();
        id.setChannelId(channelId); id.setUserId(userId);
        MessageRead mr = messageReadRepository.findById(id).orElseGet(() -> {
            MessageRead m = new MessageRead();
            m.setId(id);
            m.setChannel(new ChatChannel()); m.getChannel().setId(channelId);
            m.setUser(new User()); m.getUser().setId(userId);
            return m;
        });
        mr.setLastReadMessageId(lastReadMessageId);
        messageReadRepository.save(mr);
    }
}
