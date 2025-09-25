package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.workflow.Workflow;
import java.util.List;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    List<Workflow> findByProjectIdOrderBySortOrderAsc(Long projectId);
}
