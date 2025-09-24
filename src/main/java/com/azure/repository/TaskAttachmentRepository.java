package com.azure.repository;

import com.azure.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, com.azure.model.TaskAttachmentId> {}
