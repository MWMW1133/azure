package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.chat.ChannelMember;
import com.azure.model.chat.ChannelMemberId;
import java.util.List;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, ChannelMemberId> {
    List<ChannelMember> findById_ChannelId(Long channelId);
}
