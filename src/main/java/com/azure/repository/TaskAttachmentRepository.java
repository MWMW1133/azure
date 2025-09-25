package com.azure.repository;

import com.azure.model.TaskAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachmentEntity, Long> {
}
