package com.azure.repository;

import com.azure.model.MessageRead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReadRepository extends JpaRepository<MessageRead, com.azure.model.MessageReadId> {}
