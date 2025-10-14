package com.azure.repository;

import com.azure.model.chat.ChatChannel;
import com.azure.model.enums.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

}
