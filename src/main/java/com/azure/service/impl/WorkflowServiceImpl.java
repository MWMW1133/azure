package com.azure.service.impl;

import com.azure.model.project.Project;
import com.azure.model.workflow.Workflow;
import com.azure.repository.ProjectRepository;
import com.azure.repository.WorkflowRepository;
import com.azure.service.WorkflowService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final ProjectRepository projectRepository;

    /** 특정 프로젝트의 워크플로우 목록 조회 (페이지) */
    @Override
    @Transactional(readOnly = true)
    public Page<Workflow> listByProject(Long projectId, Pageable pageable) {
        return workflowRepository.findByProjectId(projectId, pageable);
    }

    /**
     * 워크플로우 생성
     * 규칙:
     *  - 항상 맨 끝(sortOrder 최댓값 + 1)으로 추가
     *  - 기존 terminal=true 단계들은 전부 false로 내림
     *  - 새 단계는 terminal=true로 강제 지정 (파라미터의 isTerminal 값 무시)
     */
    @Override
    public Workflow create(Long projectId, String name, int ignoredSortOrder,
                           boolean isBlocking, boolean ignoredIsTerminal, String color) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        // 1) 현재 맨 끝 sortOrder 찾기 → +1
        int nextOrder = workflowRepository.findFirstByProject_IdOrderBySortOrderDesc(projectId)
                .map(w -> (w.getSortOrder() == null ? 0 : w.getSortOrder()) + 1)
                .orElse(1);

        // 2) 기존 terminal 단계 전부 해제
        List<Workflow> terminals = workflowRepository.findByProjectIdAndIsTerminalTrue(projectId);
        if (!terminals.isEmpty()) {
            for (Workflow t : terminals) t.setIsTerminal(false);
            workflowRepository.saveAll(terminals);
        }

        // 3) 새 단계 생성: 맨 끝 + terminal=true
        Workflow wf = new Workflow();
        wf.setProject(project);
        wf.setName(name);
        wf.setSortOrder(nextOrder);
        wf.setIsBlocking(isBlocking);
        wf.setIsTerminal(true);                 // ✅ 항상 마지막 단계(완료)
        wf.setIsDefault(false);                 // 기본 단계는 건드리지 않음
        wf.setColor(color != null ? color : "#cccccc");

        return workflowRepository.save(wf);
    }

    /** 기본 4단계 보장(없으면 생성) — 기존 로직 그대로 유지 가능 */
    @Override
    @Transactional
    public void ensureDefaultStages(Long projectId) {
        if (!workflowRepository.findByProjectIdOrderBySortOrderAsc(projectId).isEmpty())
            return;

        // 기본 단계들 생성 (여기서는 terminal은 Completed에만 true)
        create(projectId, "Assignments", 1, false, false, "#e5e7eb");
        create(projectId, "In Progress", 2, false, false, "#50C7F1");
        create(projectId, "Reviewing",   3, true,  false, "#87CBFB");
        create(projectId, "Completed",   4, false, true,  "#3341FF");

        // 첫 번째 단계를 기본(isDefault=true)로 표시
        workflowRepository.findFirstByProject_IdOrderBySortOrderAsc(projectId)
                .ifPresent(w -> {
                    w.setIsDefault(true);
                    workflowRepository.save(w);
                });
    }

    /** 워크플로우 수정 */
    @Override
    public Workflow update(Long workflowId, String name, Integer sortOrder,
                           Boolean isBlocking, Boolean isTerminal, String color) {
        Workflow wf = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));

        if (name != null)       wf.setName(name);
        if (sortOrder != null)  wf.setSortOrder(sortOrder);
        if (isBlocking != null) wf.setIsBlocking(isBlocking);
        if (isTerminal != null) wf.setIsTerminal(isTerminal);
        if (color != null)      wf.setColor(color);

        return workflowRepository.save(wf);
    }

    /** 워크플로우 삭제 */
    @Override
    public void delete(Long workflowId) {
        workflowRepository.deleteById(workflowId);
    }

    /** 프로젝트 내 모든 워크플로우를 sortOrder 순서로 조회 */
    @Override
    @Transactional(readOnly = true)
    public List<Workflow> listByProjectOrdered(Long projectId) {
        return workflowRepository.findByProjectIdOrderBySortOrderAsc(projectId);
    }

    /** 기본 워크플로우 조회 (is_default = true) */
    @Override
    @Transactional(readOnly = true)
    public Optional<Workflow> findDefaultWorkflow(Long projectId) {
        return workflowRepository.findByProjectIdAndIsDefaultTrue(projectId);
    }
}