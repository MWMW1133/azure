package com.azure.repository;

import com.azure.model.chat.ChatChannel;
import com.azure.model.enums.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatChannelRepository extends JpaRepository<ChatChannel, Long> {

    // 멤버십 기반으로 내가 속한 채널
    List<ChatChannel> findByMembers_User_Id(Long userId);

    // 타입 필터(프로젝트/DM/그룹 등)
    List<ChatChannel> findByMembers_User_IdAndChannelType(Long userId, ChannelType type);
}
