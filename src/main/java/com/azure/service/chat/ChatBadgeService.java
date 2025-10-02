package com.azure.service.chat;

import com.azure.repository.MessageReadRepository;
import com.azure.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/*
 * 채널의 미읽음 메시지 개수를 계산하는 서비스.
 *
 * <p><b>알고리즘</b>
 * - message_reads 에 저장된 나의 lastReadMessageId를 구함
 * - messages 테이블에서 해당 id보다 큰 레코드 수를 세서 미읽음으로 간주
 *
 * <p><b>제공 메서드</b>
 * - {@code getUnreadCount(channelId, userId)}: 단일 채널
 * - {@code getUnreadCountMap(userId, channelIds)}: 여러 채널(A안: 채널 수만큼 반복)
 *
 * <p><b>인덱스 권장</b>
 * - messages(channel_id, id) 복합 인덱스
 * - message_reads(channel_id, user_id) 복합 인덱스
 *
 * <p><b>확장 포인트</b>
 * - N+1이 문제되면 JPQL/네이티브로 “채널별 집계 한방 쿼리” 구현체로 교체
 *   (예: Profile 'prod'에서 다른 구현체를 빈으로 등록)
 *
 * <p><b>트랜잭션</b> readOnly
 */

public class ChatBadgeService {
    private final MessageRepository messageRepo;
    private final MessageReadRepository readRepo;

    /** 지정 채널에서 '나의' 미읽음 메시지 수를 반환 (lastReadMessageId보다 큰 메시지 count). */
    public long getUnreadCount(Long channelId, Long userId) {
        long last = readRepo.findByChannel_IdAndUser_Id(channelId, userId)
                .map(r -> r.getLastReadMessageId() == null ? 0L : r.getLastReadMessageId())
                .orElse(0L);
        return messageRepo.countByChannel_IdAndIdGreaterThan(channelId, last);
    }

    /** 여러 채널의 미읽음 수를 맵으로 반환 (채널 수만큼 count 반복, N+1 허용). */
    public Map<Long, Long> getUnreadCountMap(Long userId, List<Long> channelIds) {
        var reads = readRepo.findByUser_IdAndChannel_IdIn(userId, channelIds);
        Map<Long, Long> lastMap = new HashMap<>();
        for (var r : reads) {
            lastMap.put(r.getChannel().getId(),
                    r.getLastReadMessageId() == null ? 0L : r.getLastReadMessageId());
        }
        Map<Long, Long> out = new LinkedHashMap<>();
        for (Long chId : channelIds) {
            long after = lastMap.getOrDefault(chId, 0L);
            out.put(chId, messageRepo.countByChannel_IdAndIdGreaterThan(chId, after));
        }
        return out;
    }
}
