package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.azure.model.task.Task;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    /** 특정 프로젝트의 모든 태스크 */
    List<Task> findByProjectId(Long projectId);
    /** 특정 상위 태스크의 하위 태스크들 */
    List<Task> findByParentTaskId(Long parentTaskId);
    /** 개인 태스크 조회 (project_id IS NULL) */
    Page<Task> findByProjectIdIsNullAndAssigneeId(Long userId, Pageable pageable);
}
