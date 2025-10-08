package com.azure.service.impl;

import com.azure.dto.UserRole;
import com.azure.event.ProjectMemberAddedEvent;
import com.azure.event.ProjectMemberRemovedEvent;
import com.azure.model.Organization;
import com.azure.model.project.Project;
import com.azure.model.project.ProjectMember;
import com.azure.model.project.ProjectMemberId;
import com.azure.model.user.User;
import com.azure.model.workflow.Workflow;
import com.azure.repository.OrganizationRepository;
import com.azure.repository.ProjectMemberRepository;
import com.azure.repository.ProjectRepository;
import com.azure.repository.UserRepository;
import com.azure.repository.WorkflowRepository;
import com.azure.service.ProjectService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final WorkflowRepository workflowRepository; // ✅ 추가
        private final ApplicationEventPublisher publisher; // 📢 이벤트 퍼블리셔 추가

    @Override
    @Transactional(readOnly = true)
    public Project get(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Project> listByUser(Long userId, Pageable pageable) {
        var orgs = organizationRepository.findAllByUserId(userId);
        if (orgs.isEmpty()) return Page.empty(pageable);

        var orgIds = orgs.stream().map(Organization::getId).toList();
        return projectRepository.findByOrganizationIdIn(orgIds, pageable);
    }


    @Override
    public Project create(Long organizationId, Long ownerId, String name, String description) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found: " + organizationId));
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + ownerId));

        Project p = new Project();
        p.setOrganization(org);
        p.setOwner(owner);
        p.setName(name);
        p.setDescription(description);

        Project saved = projectRepository.save(p);

        // ✅ 기본 워크플로우 자동 생성
        Workflow wf1 = new Workflow();
        wf1.setProject(saved);
        wf1.setName("Assignments");
        wf1.setSortOrder(1);
        wf1.setIsBlocking(false);
        wf1.setIsTerminal(false);
        wf1.setColor("#95a5a6"); // 회색
        workflowRepository.save(wf1);

        Workflow wf2 = new Workflow();
        wf2.setProject(saved);
        wf2.setName("In-progress");
        wf2.setSortOrder(2);
        wf2.setIsBlocking(false);
        wf2.setIsTerminal(false);
        wf2.setColor("#9fd7f8ff"); // 하늘색
        workflowRepository.save(wf2);

        Workflow wf3 = new Workflow();
        wf3.setProject(saved);
        wf3.setName("Reviewing");
        wf3.setSortOrder(3);
        wf3.setIsBlocking(false);
        wf3.setIsTerminal(false);
        wf3.setColor("#12d1f3ff"); // 하늘과 파랑 그 어딘가...
        workflowRepository.save(wf3);

        Workflow wf4 = new Workflow();
        wf4.setProject(saved);
        wf4.setName("Completed");
        wf4.setSortOrder(4);
        wf4.setIsBlocking(false);
        wf4.setIsTerminal(true);
        wf4.setColor("#267cfdff"); // 파랑색
        workflowRepository.save(wf4);

        return saved;
    }

    @Override
    public Project update(Long projectId, String name, String description) {
        Project p = get(projectId);
        if (name != null) p.setName(name);
        if (description != null) p.setDescription(description);
        return projectRepository.save(p);
    }

    @Override
    public void delete(Long projectId) {
        projectRepository.delete(get(projectId));
    }

    @Override
    public ProjectMember addMember(Long projectId, Long userId, UserRole role) {
        Project project = get(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        // ✅ WorkStatus 확인 (장기휴가면 추가 불가)
        if (user.getWorkStatus() == User.WorkStatus.LONG_LEAVE) {
            throw new IllegalArgumentException("장기 휴가 중인 사용자는 프로젝트에 추가할 수 없습니다: " + user.getName());
        }

        ProjectMember pm = new ProjectMember();
        pm.setId(new ProjectMemberId());
        pm.getId().setProjectId(project.getId());
        pm.getId().setUserId(user.getId());
        pm.setProject(project);
        pm.setUser(user);
        pm.setRole(role);
                ProjectMember saved = projectMemberRepository.save(pm);

        // 📢 이벤트 발행 (멤버 추가 알림)
        publisher.publishEvent(new ProjectMemberAddedEvent(projectId, userId, project.getOwner().getId()));

        return saved;
    }

    @Override
    public void removeMember(Long projectId, Long userId, Long removedByUserId) {
        ProjectMemberId id = new ProjectMemberId();
        id.setProjectId(projectId);
        id.setUserId(userId);
        projectMemberRepository.deleteById(id);
        // 📢 이벤트 발행 (멤버 제거 알림)
        publisher.publishEvent(new ProjectMemberRemovedEvent(projectId, userId, get(projectId).getOwner().getId()));
        
    }   
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsMember(Long projectId, Long userId) {
        return projectMemberRepository.existsById_ProjectIdAndId_UserId(projectId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectMember> listMembers(Long projectId, Pageable pageable) {
        return projectMemberRepository.findById_ProjectId(projectId, pageable);
    }
}