package com.azure.repository;

import com.azure.model.chat.ChannelMember;
import com.azure.model.chat.ChannelMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, ChannelMemberId> {

    // ChatServiceImpl.isMember(...)에서 사용
    boolean existsById_ChannelIdAndId_UserId(Long channelId, Long userId);

    // 멤버 전체 / 내 멤버십 (다른 서비스에서 사용)
    List<ChannelMember> findByChannel_Id(Long channelId);
    List<ChannelMember> findByUser_Id(Long userId);
}
