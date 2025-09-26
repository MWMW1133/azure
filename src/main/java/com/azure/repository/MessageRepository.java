package com.azure.repository;

import com.azure.model.chat.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // ★변경: 연관 경로(channel.id)에 맞게 메서드명 수정
    Page<Message> findByChannel_IdOrderByIdAsc(Long channelId, Pageable pageable);

    // (선택) 임시 페이징으로 쓰고 싶으면 리스트 버전도 추가
    // List<Message> findByChannel_IdOrderByIdAsc(Long channelId);
}
