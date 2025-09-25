package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.task.TaskAttachment;
import com.azure.model.task.TaskAttachmentId;
import java.util.List;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, TaskAttachmentId> {
    List<TaskAttachment> findById_TaskId(Long taskId);
}
