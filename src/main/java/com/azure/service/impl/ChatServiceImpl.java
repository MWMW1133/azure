package com.azure.service.impl;

import com.azure.model.chat.*;
import com.azure.model.enums.ChannelType;
import com.azure.model.file.FileObject;
import com.azure.model.project.Project;
import com.azure.model.user.User;
import com.azure.repository.*;
import com.azure.service.ChatService;
import com.azure.service.exception.BadRequestException;   // ★ 추가: 입력 검증용
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 채팅 서비스 구현.
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>메시지 읽음 표시(MessageRead)는 upsert 방식으로 저장</li>
 *   <li>채널 접근/메시지 전송/읽음표시 등은 채널 멤버만 가능(서비스에서 1차 체크) ★</li>
 *   <li>파일/답글 참조는 존재 여부 + 동일 채널 여부 확인 ★</li>
 * </ul>
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

    // ───────────────────────── 내부 유틸(멤버십/검증) ─────────────────────────

    /** 해당 사용자가 채널 멤버인지 검사 (ChannelMemberRepository의 파생쿼리 활용) ★ */
    private boolean isMember(Long channelId, Long userId) {
        return channelMemberRepository.existsById_ChannelIdAndId_UserId(channelId, userId);
    }


    // ───────────────────────── 채널 ─────────────────────────

    @Override
    public ChatChannel createChannel(ChannelType type, Long projectId, String name, Long createdBy) {
        // ★ 입력 검증
        if (type == null) throw new BadRequestException("Channel type은 필수입니다.");
        if (name == null || name.isBlank()) throw new BadRequestException("채널 이름은 필수입니다.");

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

        ChannelMemberId id = new ChannelMemberId();
        id.setChannelId(channel.getId());
        id.setUserId(user.getId());

        // ★ 멱등 처리: 이미 멤버면 조용히 반환(또는 예외 대신 무시)
        if (channelMemberRepository.existsById(id)) return;

        ChannelMember m = new ChannelMember();
        m.setId(id); m.setChannel(channel); m.setUser(user);
        channelMemberRepository.save(m);
    }

    @Override
    public void removeMember(Long channelId, Long userId) {
        ChannelMemberId id = new ChannelMemberId();
        id.setChannelId(channelId); id.setUserId(userId);
        channelMemberRepository.deleteById(id); // 존재하지 않아도 조용히 처리(멱등)
    }

    // ───────────────────────── 메시지 ─────────────────────────

    @Override
    public Message postMessage(Long channelId, Long authorId, String body, Long fileId, Long replyToId) {
        ChatChannel channel = chatChannelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("Channel not found: " + channelId));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found: " + authorId));

        // ★ 멤버십 검사: 채널 멤버만 전송 허용
        if (!isMember(channelId, authorId)) {
            throw new BadRequestException("채널 멤버만 메시지를 보낼 수 있습니다.");
        }

        // ★ 내용 검증(빈 문자열/공백 불가)
        if (body == null || body.isBlank()) {
            throw new BadRequestException("메시지 내용은 비어 있을 수 없습니다.");
        }

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
            // ★ 동일 채널 참조만 허용
            if (!ref.getChannel().getId().equals(channelId)) {
                throw new BadRequestException("다른 채널의 메시지를 답글로 참조할 수 없습니다.");
            }
            msg.setReplyTo(ref);
        }

        return messageRepository.save(msg);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> listMessages(Long channelId, Pageable pageable) {
        // ★변경: 레포지토리의 DB 페이징 메서드 직접 호출
        return messageRepository.findByChannel_IdOrderByIdAsc(channelId, pageable);
    }


    // ───────────────────────── 읽음표시 ─────────────────────────

    @Override
    public void markRead(Long channelId, Long userId, Long lastReadMessageId) {
        // ★ 멤버십 검사
        if (!isMember(channelId, userId)) {
            throw new BadRequestException("채널 멤버만 읽음 표시를 업데이트할 수 있습니다.");
        }

        MessageReadId id = new MessageReadId();
        id.setChannelId(channelId); id.setUserId(userId);

        MessageRead mr = messageReadRepository.findById(id).orElseGet(() -> {
            MessageRead m = new MessageRead();
            m.setId(id);
            m.setChannel(new ChatChannel()); m.getChannel().setId(channelId);
            m.setUser(new User()); m.getUser().setId(userId);
            return m;
        });

        // ★ lastReadMessageId가 주어졌다면 동일 채널의 메시지인지 확인(무결성)
        if (lastReadMessageId != null) {
            Message msg = messageRepository.findById(lastReadMessageId)
                    .orElseThrow(() -> new NotFoundException("Message not found: " + lastReadMessageId));
            if (!msg.getChannel().getId().equals(channelId)) {
                throw new BadRequestException("다른 채널 메시지를 읽음 위치로 설정할 수 없습니다.");
            }
            mr.setLastReadMessageId(lastReadMessageId);
        }

        messageReadRepository.save(mr);
    }
}
