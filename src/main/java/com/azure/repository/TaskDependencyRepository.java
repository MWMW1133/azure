package com.azure.repository;

import com.azure.model.TaskDependencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskDependencyRepository extends JpaRepository<TaskDependencyEntity, Long> {
}
