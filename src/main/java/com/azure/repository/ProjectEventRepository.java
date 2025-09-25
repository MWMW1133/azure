package com.azure.repository;

import com.azure.model.ProjectEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectEventRepository extends JpaRepository<ProjectEventEntity, Long> {
}
