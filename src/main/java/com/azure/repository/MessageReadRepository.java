package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.chat.MessageRead;
import com.azure.model.chat.MessageReadId;
import java.util.List;

public interface MessageReadRepository extends JpaRepository<MessageRead, MessageReadId> {
    List<MessageRead> findById_ChannelId(Long channelId);
}
