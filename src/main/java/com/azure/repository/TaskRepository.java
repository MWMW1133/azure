package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.task.Task;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
}
