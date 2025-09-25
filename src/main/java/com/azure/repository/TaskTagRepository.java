package com.azure.repository;

import com.azure.model.TaskTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskTagRepository extends JpaRepository<TaskTagEntity, Long> {
}
