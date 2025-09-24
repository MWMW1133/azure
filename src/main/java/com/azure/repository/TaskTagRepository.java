package com.azure.repository;

import com.azure.model.TaskTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskTagRepository extends JpaRepository<TaskTag, com.azure.model.TaskTagId> {}
