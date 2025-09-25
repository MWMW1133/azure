package com.azure.repository;

import com.azure.model.MessageReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReadRepository extends JpaRepository<MessageReadEntity, Long> {
}
