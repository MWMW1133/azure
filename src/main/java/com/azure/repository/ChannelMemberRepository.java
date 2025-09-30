package com.azure.repository;

import com.azure.model.chat.ChannelMember;
import com.azure.model.chat.ChannelMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, ChannelMemberId> {

    @Query("select cm.user.id from ChannelMember cm where cm.channel.id = :channelId")
    List<ChannelMember> findById_ChannelId(Long channelId);

    // ★ 멤버십 O(1) 체크용 (빠름)
    boolean existsById_ChannelIdAndId_UserId(Long channelId, Long userId); // ★
}
