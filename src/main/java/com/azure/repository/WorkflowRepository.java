package com.azure.repository;

import com.azure.model.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository extends JpaRepository<WorkflowStep, Long> {}
