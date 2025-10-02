package com.azure.service.chat;

import com.azure.model.chat.ChatChannel;
import com.azure.model.enums.ChannelType;
import com.azure.repository.ChatChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

/**
 * 사용자가 속한 채널 목록을 조회하고, 각 채널의 미읽음 개수를 합쳐
 * 채팅방 사이드바에 바로 뿌릴 수 있는 요약 DTO로 만들어주는 서비스.
 *
 * <p><b>무엇을 하나요?</b>
 * - (A안) JPA 파생 메서드로 내가 속한 채널을 전부 조회
 * - 프로젝트 채널 우선(null 우선) → 이름 순으로 정렬
 * - {@link ChatBadgeService} 를 호출해 채널별 미읽음(unread) 카운트를 합성
 *
 * <p><b>언제 쓰나요?</b>
 * - 좌측 사이드바 채널 리스트를 그릴 때
 * - 채널 타입별(프로젝트/DM/그룹) 필터가 필요할 때
 *
 * <p><b>입/출력</b>
 * - in : userId, (선택) ChannelType
 * - out: ChannelSummary(id, name, type, projectId, unread)
 *
 * <p><b>성능 주의</b>
 * - A안이므로 채널 수만큼 {@code count} 쿼리가 반복될 수 있음(N+1).
 * - 소규모/데모엔 충분. 규모가 커지면 B안(JPQL 집계 한 방)으로 교체 권장.
 *
 * <p><b>트랜잭션</b> readOnly
 */

// 내 채널 목록(+뱃지 합성, 정렬만 자바에서)
public class ChannelListService {
    private final ChatChannelRepository channelRepo;
    private final ChatBadgeService badgeService;

    /** 내가 속한 채널 목록을 조회→정렬→미읽음 합성하여 사이드바용 요약 DTO를 반환. */
    public List<ChannelSummary> listMyChannels(Long userId, @org.springframework.lang.Nullable ChannelType type) {
        var channels = (type == null)
                ? channelRepo.findByMembers_User_Id(userId)
                : channelRepo.findByMembers_User_IdAndChannelType(userId, type);

        // 프로젝트(null 우선) → 이름순
        channels.sort(
                Comparator.comparing((ChatChannel c) -> c.getProject() == null ? 0 : 1)
                        .thenComparing(c -> Optional.ofNullable(c.getName()).orElse(""),
                                String.CASE_INSENSITIVE_ORDER)
        );

        var ids = channels.stream().map(ChatChannel::getId).toList();
        var unread = badgeService.getUnreadCountMap(userId, ids);

        return channels.stream().map(c -> new ChannelSummary(
                c.getId(),
                c.getName(),
                c.getChannelType(),
                c.getProject() == null ? null : c.getProject().getId(),
                unread.getOrDefault(c.getId(), 0L)
        )).toList();
    }

    public record ChannelSummary(
            Long id, String name, ChannelType type, Long projectId, Long unread
    ) {}
}

