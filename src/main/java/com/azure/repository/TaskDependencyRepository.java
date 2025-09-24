package com.azure.repository;

import com.azure.model.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, com.azure.model.TaskDependencyId> {}
