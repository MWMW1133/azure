package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.task.TaskAttachment;
import com.azure.model.task.TaskAttachmentId;
import java.util.List;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, TaskAttachmentId> {
    // Task ID로 첨부파일 조회
    List<TaskAttachment> findById_TaskId(Long taskId);
    // File ID로 첨부파일 조회
    List<TaskAttachment> findById_FileId(Long fileId);
}
