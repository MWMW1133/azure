package com.azure.repository;

import com.azure.model.ChatChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatChannelRepository extends JpaRepository<ChatChannel, Long> {}
