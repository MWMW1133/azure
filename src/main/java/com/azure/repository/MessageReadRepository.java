package com.azure.repository;

import com.azure.model.chat.MessageRead;
import com.azure.model.chat.MessageReadId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageReadRepository extends JpaRepository<MessageRead, MessageReadId> {
    Optional<MessageRead> findByChannel_IdAndUser_Id(Long channelId, Long userId);
    List<MessageRead> findByUser_IdAndChannel_IdIn(Long userId, List<Long> channelIds);
}
