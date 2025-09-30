package com.azure.repository;

import com.azure.model.chat.ChannelMember;
import com.azure.model.chat.ChannelMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, ChannelMemberId> {

    List<ChannelMember> findById_ChannelId(Long channelId);

    // ★ 멤버십 O(1) 체크용 (빠름)
    boolean existsById_ChannelIdAndId_UserId(Long channelId, Long userId); // ★
}
