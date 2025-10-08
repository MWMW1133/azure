package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.task.TaskDependency;
import java.util.List;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    // Predecessor ID로 종속성 조회
    List<TaskDependency> findByPredecessorId(Long predecessorId);
    //  Successor ID로 종속성 조회
    List<TaskDependency> findBySuccessorId(Long successorId);
}
