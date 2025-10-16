package com.azure.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.azure.dto.ProjectChatDTO;
import com.azure.event.ChatMessageCreatedEvent;
import com.azure.model.chat.ChannelMember;
import com.azure.model.chat.ChannelMemberId;
import com.azure.model.chat.ChatChannel;
import com.azure.model.chat.Message;
import com.azure.model.chat.MessageRead;
import com.azure.model.chat.MessageReadId;
import com.azure.model.enums.ChannelType;
import com.azure.model.file.FileObject;
import com.azure.model.project.Project;
import com.azure.model.user.User;
import com.azure.repository.ChannelMemberRepository;
import com.azure.repository.ChatChannelRepository;
import com.azure.repository.FileObjectRepository;
import com.azure.repository.MessageReadRepository;
import com.azure.repository.MessageRepository;
import com.azure.repository.ProjectRepository;
import com.azure.repository.UserRepository;
import com.azure.service.ChatService;
import com.azure.service.chat.ChatNlpService;
import com.azure.service.exception.BadRequestException;
import com.azure.service.exception.NotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    private final ChatNlpService chatNlpService;
    private final ProjectRepository projectRepository;

    @PersistenceContext
    private EntityManager em;

    /** 디버그용 강제 번역 (application.properties: mt.force=true) */
    @Value("${mt.force:false}")
    private boolean mtForce;

    // ───────────── 내부 유틸 ─────────────

    private boolean isMember(Long channelId, Long userId) {
        return channelMemberRepository.existsById_ChannelIdAndId_UserId(channelId, userId);
    }

    private String normalizeTarget(String t) {
        if (t == null) return "en";
        String s = t.trim().toLowerCase();
        return switch (s) {
            case "en", "english", "영어" -> "en";
            case "ko", "korean", "한국어", "한글" -> "ko";
            case "ja", "japanese", "일본어" -> "ja";
            case "zh", "chinese", "중국어", "zh-cn", "cn" -> "zh";
            default -> "en";
        };
    }

    // ───────────── DM 채널 ─────────────

    @Override
    public Long getOrCreateDmChannel(long me, long peer) {
        if (me == peer) throw new BadRequestException("me == peer");
        final long a = Math.min(me, peer);
        final long b = Math.max(me, peer);

        var found = chatChannelRepository.findDmChannelIdByTwoMembers(a, b);
        if (found.isPresent()) return found.get();

        var foundReverse = chatChannelRepository.findDmChannelIdByTwoMembers(b, a);
        if (foundReverse.isPresent()) return foundReverse.get();

        var meUser = userRepository.findById(me).orElseThrow(() -> new NotFoundException("me not found"));
        var peerUser = userRepository.findById(peer).orElseThrow(() -> new NotFoundException("peer not found"));

        try {
            ChatChannel dm = new ChatChannel();
            dm.setChannelType(ChannelType.DM);
            dm.setName("DM:" + a + ":" + b);

            ChatChannel saved = chatChannelRepository.save(dm);
            addMember(saved.getId(), meUser.getId());
            addMember(saved.getId(), peerUser.getId());
            return saved.getId();
        } catch (DataIntegrityViolationException e) {
            return chatChannelRepository.findDmChannelIdByTwoMembers(a, b)
                    .or(() -> chatChannelRepository.findDmChannelIdByTwoMembers(b, a))
                    .orElseThrow(() -> e);
        }
    }

    // ───────────── 채널 ─────────────

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
        channelMemberRepository.deleteById(id);
    }

    // ───────────── 메시지 ─────────────

    @Override
    public Message postMessage(Long channelId, Long authorId, String body, Long fileId, Long replyToId) {
        return postMessage(channelId, authorId, body, fileId, replyToId, Boolean.FALSE, "en");
    }

    @Override
    public Message postMessage(Long channelId, Long authorId, String body, Long fileId, Long replyToId,
                               Boolean translateEnabled, String targetLang) {
        ChatChannel channel = chatChannelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("Channel not found: " + channelId));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found: " + authorId));

        if (!isMember(channelId, authorId)) throw new BadRequestException("채널 멤버만 메시지를 보낼 수 있습니다.");
        if (body == null || body.isBlank()) throw new BadRequestException("메시지 내용은 비어 있을 수 없습니다.");

        String normalized = normalizeTarget(targetLang);
        boolean doTranslate = mtForce || Boolean.TRUE.equals(translateEnabled);
        String finalBody = doTranslate ? chatNlpService.translate(body, normalized) : body;

        Message msg = new Message();
        msg.setChannel(channel);
        msg.setAuthor(author);
        msg.setBody(finalBody);

        if (fileId != null) {
            FileObject f = fileObjectRepository.findById(fileId)
                    .orElseThrow(() -> new NotFoundException("File not found: " + fileId));
            msg.setFile(f);
        }
        if (replyToId != null) {
            Message ref = messageRepository.findById(replyToId)
                    .orElseThrow(() -> new NotFoundException("Message not found: " + replyToId));
            if (!ref.getChannel().getId().equals(channelId))
                throw new BadRequestException("다른 채널 메시지를 답글로 참조할 수 없습니다.");
            msg.setReplyTo(ref);
        }

        Message saved = messageRepository.save(msg);
        String preview = finalBody.length() > 20 ? finalBody.substring(0, 20) + "..." : finalBody;
        publisher.publishEvent(new ChatMessageCreatedEvent(channelId, saved.getId(), authorId, preview));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> listMessages(Long channelId, Pageable pageable) {
        return messageRepository.findByChannel_IdOrderByIdAsc(channelId, pageable);
    }

    @Override
    public void markRead(Long channelId, Long userId, Long lastReadMessageId) {
        if (!isMember(channelId, userId)) throw new BadRequestException("채널 멤버만 읽음 표시를 업데이트할 수 있습니다.");

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
            if (!msg.getChannel().getId().equals(channelId))
                throw new BadRequestException("다른 채널 메시지를 읽음 위치로 설정할 수 없습니다.");

            Long prev = mr.getLastReadMessageId();
            if (prev == null || lastReadMessageId > prev) mr.setLastReadMessageId(lastReadMessageId);
        }

        messageReadRepository.save(mr);
    }

    // ───────────── 프로젝트 채널/목록 ─────────────

    @Override
    public Long getOrCreateProjectChannel(Long projectId, String projectName, Long createdBy) {
        if (projectId == null) throw new BadRequestException("projectId is required");

        var existing = chatChannelRepository
                .findByMembers_User_IdAndChannelType(createdBy, ChannelType.PROJECT)
                .stream()
                .filter(c -> c.getProject() != null && projectId.equals(c.getProject().getId()))
                .findFirst();
        if (existing.isPresent()) return existing.get().getId();

        Project prj = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        ChatChannel ch = new ChatChannel();
        ch.setChannelType(ChannelType.PROJECT);
        ch.setProject(prj);
        ch.setName((projectName != null && !projectName.isBlank()) ? projectName : prj.getName());

        if (createdBy != null) {
            User creator = userRepository.findById(createdBy)
                    .orElseThrow(() -> new NotFoundException("User not found: " + createdBy));
            ch.setCreatedBy(creator);
        }

        ChatChannel saved = chatChannelRepository.save(ch);
        if (createdBy != null) addMember(saved.getId(), createdBy);
        return saved.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectChatDTO> listProjectRooms(Long userId) {
        if (userId == null) throw new BadRequestException("userId is required");

        String sql = """
            SELECT  p.id        AS project_id,
                    p.name      AS name,
                    (
                      SELECT c.id
                      FROM chat_channels c
                      WHERE c.project_id = p.id
                        AND c.channel_type = 'PROJECT'
                      ORDER BY c.id
                      LIMIT 1
                    )           AS channel_id
            FROM projects p
            JOIN project_members pm
              ON pm.project_id = p.id
            WHERE pm.user_id = :uid
            ORDER BY p.id
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("uid", userId)
                .getResultList();

        List<ProjectChatDTO> list = new ArrayList<>();
        for (Object[] r : rows) {
            Long projectId = ((Number) r[0]).longValue();
            String name    = (String) r[1];
            Long channelId = (r[2] == null) ? null : ((Number) r[2]).longValue();
            list.add(new ProjectChatDTO(projectId, name, channelId));
        }
        return list;
    }
}
