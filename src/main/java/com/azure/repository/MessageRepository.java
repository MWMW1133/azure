package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.chat.Message;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChannelIdOrderByIdAsc(Long channelId);
}
