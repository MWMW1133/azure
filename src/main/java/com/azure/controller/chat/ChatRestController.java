package com.azure.controller.chat;

import com.azure.dto.MessageDTO;
import com.azure.model.chat.ChatChannel;
import com.azure.model.chat.Message;
import com.azure.model.enums.ChannelType;
import com.azure.model.user.User;
import com.azure.repository.ChatChannelRepository;
import com.azure.repository.UserRepository;
import com.azure.service.ChatService;
import com.azure.support.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final UserRepository userRepo;
    private final CurrentUserResolver currentUserResolver;

    // 채널 목록 조회용
    private final ChatChannelRepository channelRepo;

    // ───────────────────────────────────────────────────────────────────────────
    // 채널 목록 조회 (DM / PROJECT)
    //   GET /api/channels?type=PROJECT   (type=DM 도 가능)
    //   scope=PROJECT 로 와도 동일하게 동작하게 허용
    // ───────────────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> myChannels(
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "scope", required = false) String scope, // 호환용
            Principal principal,
            HttpSession session) {

        // 로그인 사용자
        User me = currentUserResolver.resolve(principal, session);
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "UNAUTHORIZED",
                    "message", "로그인이 필요합니다."
            ));
        }

        // type 우선, 없으면 scope 사용
        String want = (type != null && !type.isBlank()) ? type : scope;

        List<ChatChannel> list;
        if (want == null || want.isBlank()) {
            list = channelRepo.findByMembers_User_Id(me.getId());
        } else {
            ChannelType chType;
            try {
                chType = ChannelType.valueOf(want.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "BAD_REQUEST",
                        "message", "type(scope)은 PROJECT 또는 DM 이어야 합니다."
                ));
            }
            list = channelRepo.findByMembers_User_IdAndChannelType(me.getId(), chType);
        }

        // 응답 변환 (Map.of 대신 HashMap으로 교차타입 이슈 회피)
        List<Map<String, Object>> rows = list.stream()
                .map(ch -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", ch.getId());
                    m.put("name", ch.getName());
                    m.put("type", ch.getChannelType().name());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(rows);
    }

    /** DM 채널 보장 */
    @RequestMapping(value = "/dm/{peerId}/channel", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> dmChannel(@PathVariable Long peerId,
                                       Principal principal,
                                       HttpSession session) {
        User me = currentUserResolver.resolve(principal, session);
        if (me == null) {
            log.warn("[DM] no auth: principal={}, session user=null", principal != null ? principal.getName() : "null");
            return ResponseEntity.status(401).body(Map.of(
                    "error", "UNAUTHORIZED",
                    "message", "로그인이 필요합니다."
            ));
        }
        Long meId = me.getId();

        // 자기자신 금지
        if (meId.equals(peerId)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "SELF_DM_NOT_ALLOWED",
                    "message", "자기 자신에게는 DM을 보낼 수 없습니다."
            ));
        }

        // 상대 존재 확인
        if (!userRepo.existsById(peerId)) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "PEER_NOT_FOUND",
                    "message", "상대 사용자를 찾을 수 없습니다.",
                    "peerId", peerId
            ));
        }

        // 채널 조회/생성
        try {
            Long chId = chatService.getOrCreateDmChannel(meId, peerId);
            return ResponseEntity.ok(Map.of("channelId", chId));
        } catch (Exception e) {
            log.error("[DM] getOrCreateDmChannel failed: meId={}, peerId={}", meId, peerId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "DM_CREATE_FAILED",
                    "message", "DM 채널 생성/조회 중 오류가 발생했습니다."
            ));
        }
    }

    /** 채널 메시지 로드 (그룹/DM 공통) */
    @GetMapping("/{channelId}/messages")
    public List<MessageDTO> recentMessages(@PathVariable Long channelId,
                                           @RequestParam(defaultValue = "50") int limit) {
        int size = Math.min(Math.max(limit, 1), 200);
        var page = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Message> result = chatService.listMessages(channelId, page);
        var list = result.getContent().stream().map(this::toDto).collect(Collectors.toList());
        Collections.reverse(list); // 오래된 → 최신
        return list;
    }

    private MessageDTO toDto(Message m) {
        MessageDTO dto = new MessageDTO();
        dto.setId(m.getId());
        dto.setChannelId(m.getChannel() != null ? m.getChannel().getId() : null);
        dto.setAuthorId(m.getAuthor() != null ? m.getAuthor().getId() : null);
        dto.setBody(m.getBody());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
