package com.azure.service;

import com.azure.model.workflow.Workflow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkflowService {
    Page<Workflow> listByProject(Long projectId, Pageable pageable);

    Workflow create(Long projectId, String name, int sortOrder, boolean isBlocking, boolean isTerminal, String color);

    Workflow update(Long workflowId, String name, Integer sortOrder, Boolean isBlocking, Boolean isTerminal, String color);

    void delete(Long workflowId);
}