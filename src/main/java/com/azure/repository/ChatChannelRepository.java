package com.azure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.azure.model.chat.ChatChannel;
import com.azure.model.enums.ChannelType;

public interface ChatChannelRepository extends JpaRepository<ChatChannel, Long> {

    List<ChatChannel> findByMembers_User_Id(Long userId);
    List<ChatChannel> findByMembers_User_IdAndChannelType(Long userId, ChannelType type);

    // DM은 "DM:<작은ID>:<큰ID>" 이름 규칙으로 조회
    Optional<ChatChannel> findByNameAndChannelType(String name, ChannelType type);

    @Query("""
      select c.id
      from ChatChannel c
        join c.members m
      where c.channelType = com.azure.model.enums.ChannelType.DM
        and m.user.id in (:u1, :u2)
      group by c.id
      having count(distinct m.user.id) = 2
    """)
    Optional<Long> findDmChannelIdByTwoMembers(@Param("u1") Long u1, @Param("u2") Long u2);

    // ====== ✅ 프로젝트 기반 채널 조회 추가 ======

    /** 해당 프로젝트에 연결된 채널이 있으면 반환 */
    Optional<ChatChannel> findByProject_Id(Long projectId);

    /** 해당 프로젝트의 채널 ID만 빠르게 조회 (없으면 empty) */
    @Query("select c.id from ChatChannel c where c.project.id = :projectId")
    Optional<Long> findIdByProjectId(@Param("projectId") Long projectId);
}
