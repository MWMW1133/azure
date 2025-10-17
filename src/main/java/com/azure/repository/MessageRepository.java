package com.azure.repository;

import com.azure.model.chat.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 목록/스레드/카운트(미읽음)
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByChannel_Id(Long channelId, Pageable pageable);

    List<Message> findByReplyTo_IdOrderByIdAsc(Long parentMessageId);
    long countByChannel_IdAndIdGreaterThan(Long channelId, Long afterMessageId);
}
