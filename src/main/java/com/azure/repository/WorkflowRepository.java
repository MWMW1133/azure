package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.azure.model.workflow.Workflow;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    /** 특정 프로젝트에 속한 워크플로우 목록 페이징 조회 */
    Page<Workflow> findByProjectId(Long projectId, Pageable pageable);
    ///** 특정 프로젝트에 속한 워크플로우 목록 (페이징 없이) */
    List<Workflow> findByProjectIdAndIsTerminalTrue(Long projectId);
    // 특정 프로젝트에 속한 기본 워크플로우 조회
    Optional<Workflow> findByProjectIdAndIsDefaultTrue(Long projectId);
    // 프로젝트 내 워크플로우를 sortOrder 순서로 모두 조회
    List<Workflow> findByProjectIdOrderBySortOrderAsc(Long projectId);

    /** 이름으로 워크플로 찾을 때 사용 (changeWorkflow 보조용) */
    Optional<Workflow> findByProjectIdAndName(Long projectId, String name);
    /** 프로젝트의 첫 번째 워크플로우 (sortOrder 기준) */
    Optional<Workflow> findFirstByProject_IdOrderBySortOrderAsc(Long projectId);
}
