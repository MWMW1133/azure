package com.azure.service;

import com.azure.model.workflow.Workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkflowService {
    // ID로 워크플로우 조회
    Page<Workflow> listByProject(Long projectId, Pageable pageable);
    // 워크플로우 생성
    Workflow create(Long projectId, String name, int sortOrder, boolean isBlocking, boolean isTerminal, String color);
    // 워크플로우 수정 (부분 수정 허용)
    Workflow update(Long workflowId, String name, Integer sortOrder, Boolean isBlocking, Boolean isTerminal, String color);
    // 워크플로우 삭제 (FK 제약조건에 따라 실패할 수 있음)
    void delete(Long workflowId);
    // 프로젝트 내 워크플로우를 sortOrder 순서로 모두 조회
    List<Workflow> listByProjectOrdered(Long projectId);
    // 기본 워크플로우 조회 (is_default = true)
    Optional<Workflow> findDefaultWorkflow(Long projectId);
}