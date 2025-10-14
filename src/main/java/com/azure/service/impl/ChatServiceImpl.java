package com.azure.service.impl;

import com.azure.event.ChatMessageCreatedEvent;
import com.azure.model.chat.*;
import com.azure.model.enums.ChannelType;
import com.azure.model.file.FileObject;
import com.azure.model.project.Project;
import com.azure.model.user.User;
import com.azure.repository.*;
import com.azure.service.ChatService;
import com.azure.service.exception.BadRequestException;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException; // ★ 중복 생성 경합 처리
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
 *   <li>채널 접근/메시지 전송/읽음표시 등은 채널 멤버만 가능(서비스에서 1차 체크)</li>
 *   <li>파일/답글 참조는 존재 여부 + 동일 채널 여부 확인</li>
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
    private final FileObjectRepository fileObjectRepository;
    private final ApplicationEventPublisher publisher;

    // ───────────────────────── 내부 유틸(멤버십/검증) ─────────────────────────

    /** 해당 사용자가 채널 멤버인지 검사 */
    private boolean isMember(Long channelId, Long userId) {
        return channelMemberRepository.existsById_ChannelIdAndId_UserId(channelId, userId);
    }

    // ───────────────────────── DM 채널 get-or-create ─────────────────────────
    /**
     * 두 사용자(meId, peerId) 사이의 DM 채널을 찾고, 없으면 생성해서 채널 ID 반환.
     * - (a,b) 순으로 정규화해 중복 생성을 방지
     * - 동시 생성 경합은 DataIntegrityViolationException 캐치 후 재조회
     */
    @Override
    public Long getOrCreateDmChannel(long me, long peer) {
        if (me == peer) {
            throw new BadRequestException("me == peer");
        }

        // 1) 정규화 (작은 id, 큰 id)
        final long a = Math.min(me, peer);
        final long b = Math.max(me, peer);

        // 2) 기존 채널 조회 (양 방향 메서드를 모두 시도)
        //   - 프로젝트에 이미 존재하는 메서드 시그니처에 맞춰 호출
        var found = chatChannelRepository.findDmChannelIdByTwoMembers(a, b);
        if (found.isPresent()) return found.get();

        // 혹시 구현이 (me,peer) 순서로만 되어 있다면 역순도 시도
        var foundReverse = chatChannelRepository.findDmChannelIdByTwoMembers(b, a);
        if (foundReverse.isPresent()) return foundReverse.get();

        // 3) 없으면 생성 (유저 존재 확인)
        var meUser   = userRepository.findById(me)
                .orElseThrow(() -> new NotFoundException("me not found"));
        var peerUser = userRepository.findById(peer)
                .orElseThrow(() -> new NotFoundException("peer not found"));

        try {
            ChatChannel dm = new ChatChannel();
            dm.setChannelType(ChannelType.DM);
            // 이름은 가벼운 식별용(실제 표시는 프론트에서 상대 이름 사용)
            dm.setName("DM:" + a + ":" + b);

            ChatChannel saved = chatChannelRepository.save(dm);

            // 멤버 두 명 추가(멱등)
            addMember(saved.getId(), meUser.getId());
            addMember(saved.getId(), peerUser.getId());

            return saved.getId();
        } catch (DataIntegrityViolationException e) {
            // 동시 생성 경합 발생 시 재조회하여 id 반환
            var again = chatChannelRepository.findDmChannelIdByTwoMembers(a, b);
            if (again.isPresent()) return again.get();
            var again2 = chatChannelRepository.findDmChannelIdByTwoMembers(b, a);
            if (again2.isPresent()) return again2.get();
            throw e; // 정말 없다면 원인 파악 위해 그대로 던짐
        }
    }

    // ───────────────────────── 채널 ─────────────────────────

    @Override
    public ChatChannel createChannel(ChannelType type, Long projectId, String name, Long createdBy) {
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

        // 멱등 처리: 이미 멤버면 조용히 반환
        if (channelMemberRepository.existsById(id)) return;

        ChannelMember m = new ChannelMember();
        m.setId(id);
        m.setChannel(channel);
        m.setUser(user);
        channelMemberRepository.save(m);
    }

    @Override
    public void removeMember(Long channelId, Long userId) {
        ChannelMemberId id = new ChannelMemberId();
        id.setChannelId(channelId);
        id.setUserId(userId);
        channelMemberRepository.deleteById(id); // 존재하지 않아도 멱등
    }

    // ───────────────────────── 메시지 ─────────────────────────

    @Override
    public Message postMessage(Long channelId, Long authorId, String body, Long fileId, Long replyToId) {
        ChatChannel channel = chatChannelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("Channel not found: " + channelId));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found: " + authorId));

        if (!isMember(channelId, authorId)) {
            throw new BadRequestException("채널 멤버만 메시지를 보낼 수 있습니다.");
        }
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
            if (!ref.getChannel().getId().equals(channelId)) {
                throw new BadRequestException("다른 채널 메시지를 답글로 참조할 수 없습니다.");
            }
            msg.setReplyTo(ref);
        }

        Message saved = messageRepository.save(msg);

        // 알림 이벤트 발행
        String preview = body.length() > 20 ? body.substring(0, 20) + "..." : body;
        publisher.publishEvent(new ChatMessageCreatedEvent(channelId, saved.getId(), authorId, preview));

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> listMessages(Long channelId, Pageable pageable) {
        return messageRepository.findByChannel_IdOrderByIdAsc(channelId, pageable);
    }

    // ───────────────────────── 읽음표시 ─────────────────────────

    @Override
    public void markRead(Long channelId, Long userId, Long lastReadMessageId) {
        if (!isMember(channelId, userId)) {
            throw new BadRequestException("채널 멤버만 읽음 표시를 업데이트할 수 있습니다.");
        }

        MessageReadId id = new MessageReadId();
        id.setChannelId(channelId);
        id.setUserId(userId);

        MessageRead mr = messageReadRepository.findById(id).orElseGet(() -> {
            MessageRead m = new MessageRead();
            m.setId(id);
            m.setChannel(new ChatChannel()); m.getChannel().setId(channelId);
            m.setUser(new User()); m.getUser().setId(userId);
            return m;
        });

        if (lastReadMessageId != null) {
            Message msg = messageRepository.findById(lastReadMessageId)
                    .orElseThrow(() -> new NotFoundException("Message not found: " + lastReadMessageId));
            if (!msg.getChannel().getId().equals(channelId)) {
                throw new BadRequestException("다른 채널 메시지를 읽음 위치로 설정할 수 없습니다.");
            }
            Long prev = mr.getLastReadMessageId();
            if (prev == null || lastReadMessageId > prev) {
                mr.setLastReadMessageId(lastReadMessageId);
            }
        }

        messageReadRepository.save(mr);
    }
}
