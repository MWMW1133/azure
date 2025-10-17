package com.azure.repository;

import com.azure.model.chat.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 목록/스레드/카운트(미읽음)
public interface MessageRepository extends JpaRepository<Message, Long> {

    // ChatServiceImpl.listMessages(...)에서 사용 (정렬 포함, 페이지네이션)
    Page<Message> findByChannel_IdOrderByIdAsc(Long channelId, Pageable pageable);


    // 스레드(답글) 조회가 필요할 때 재사용
    List<Message> findByReplyTo_IdOrderByIdAsc(Long parentMessageId);

    Page<Message> findByChannel_Id(Long channelId, Pageable pageable);
    // A안: 미읽음(N+1 허용) 계산용
    long countByChannel_IdAndIdGreaterThan(Long channelId, Long afterMessageId);
}
