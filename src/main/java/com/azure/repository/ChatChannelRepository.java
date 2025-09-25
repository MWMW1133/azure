package com.azure.repository;

import com.azure.model.ChatChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatChannelRepository extends JpaRepository<ChatChannelEntity, Long> {
}
