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

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService.Work;
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

    /** 특정 프로젝트의 워크플로우 목록 조회 */
    @Override
    @Transactional(readOnly = true)
    public Page<Workflow> listByProject(Long projectId, Pageable pageable) {
        return workflowRepository.findByProjectId(projectId, pageable);
    }

    /** 워크플로우 생성 (단일 단계 추가) */
    @Override
    public Workflow create(Long projectId, String name, int sortOrder, boolean isBlocking, boolean isTerminal, String color) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));
        int nextOrder = workflowRepository.findMaxSortOrderByProjectId(projectId).orElse(0) + 1;
        Workflow wf = new Workflow();
        wf.setProject(project);
        wf.setName(name);
        wf.setSortOrder(nextOrder);
        wf.setIsBlocking(isBlocking);
        wf.setIsTerminal(false);
        wf.setColor(color != null ? color : "#cccccc");

        Workflow w = workflowRepository.save(wf);
        enforceTerminal(projectId);
        return w;
    }

    /// 워크플로우 수정
    @Override
    public Workflow update(Long workflowId, String name, Integer sortOrder, Boolean isBlocking, Boolean isTerminal, String color) {
        Workflow wf = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));

        if (name != null) wf.setName(name);
        if (sortOrder != null) wf.setSortOrder(sortOrder);
        if (isBlocking != null) wf.setIsBlocking(isBlocking);
        if (color != null) wf.setColor(color);

        return workflowRepository.save(wf);
    }

    /** 워크플로우 삭제 */
    @Override
    public void delete(Long workflowId) {
        Workflow w = workflowRepository.findById(workflowId).orElseThrow();
        Long projectId = w.getProject().getId();
        workflowRepository.delete(w);
        enforceTerminal(projectId);
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

    private void enforceTerminal(Long projectId) {
        List<Workflow> list = workflowRepository.findByProjectIdOrderBySortOrderAsc(projectId);
        if (list.isEmpty()) return;

        int lastIdx = list.size() - 1;
        for (int i = 0; i < list.size(); i++) {
            Workflow wf = list.get(i);
            boolean shouldBeTerminal = (i == lastIdx);
            if (Boolean.TRUE.equals(wf.getIsTerminal()) != shouldBeTerminal) {
                wf.setIsTerminal(shouldBeTerminal);
                workflowRepository.save(wf);
            }
        }
    }
}
