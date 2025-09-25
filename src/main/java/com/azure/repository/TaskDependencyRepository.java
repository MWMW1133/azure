package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.task.TaskDependency;
import java.util.List;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    List<TaskDependency> findByPredecessorId(Long predecessorId);
    List<TaskDependency> findBySuccessorId(Long successorId);
}
