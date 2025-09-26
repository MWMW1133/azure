package com.azure.repository;

import com.azure.model.chat.MessageRead;
import com.azure.model.chat.MessageReadId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional; // ★ 추가

public interface MessageReadRepository extends JpaRepository<MessageRead, MessageReadId> {

    // 채널의 모든 읽음표시 레코드(모든 사용자) 조회
    List<MessageRead> findById_ChannelId(Long channelId);

    // ★ 특정 채널 + 특정 사용자 단건 조회 (서비스에서 upsert 시 편함)
    Optional<MessageRead> findById_ChannelIdAndId_UserId(Long channelId, Long userId);

    // ★ 존재 여부 O(1) 체크 (권한/전처리 등에 유용)
    boolean existsById_ChannelIdAndId_UserId(Long channelId, Long userId);

    // ★ 사용자가 채널을 나갔을 때 정리할 때 유용
    void deleteById_ChannelIdAndId_UserId(Long channelId, Long userId);
}
