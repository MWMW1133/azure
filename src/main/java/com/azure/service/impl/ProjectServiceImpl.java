package com.azure.service.impl;

import com.azure.event.ProjectMemberAddedEvent;
import com.azure.event.ProjectMemberRemovedEvent;
import com.azure.model.Organization;
import com.azure.model.enums.OrganizationRole;
import com.azure.model.project.Project;
import com.azure.model.project.ProjectMember;
import com.azure.model.project.ProjectMemberId;
import com.azure.model.user.User;
import com.azure.repository.OrganizationMemberRepository;
import com.azure.repository.OrganizationRepository;
import com.azure.repository.ProjectMemberRepository;
import com.azure.repository.ProjectRepository;
import com.azure.repository.UserRepository;
import com.azure.service.ProjectService;
import com.azure.service.WorkflowService;
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
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;

    // ✅ 워크플로우 생성 책임은 전담 서비스에 위임
    private final WorkflowService workflowService;

    // 📢 프로젝트 멤버 추가/삭제 이벤트 발행
    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional(readOnly = true)
    public Project get(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Project> listByUser(Long userId, Pageable pageable) {
        var memberships = organizationMemberRepository.findByUserId(userId);
        if (memberships.isEmpty()) return Page.empty(pageable);

        var orgIds = memberships.stream()
                .map(m -> m.getOrganization().getId())
                .distinct()
                .toList();

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

        // ✅ 기본 워크플로우 4단계를 보장(없으면 생성, 있으면 스킵)
        workflowService.ensureDefaultStages(saved.getId());

        // ✅ 소유자를 프로젝트 멤버로 자동 등록 (없을 때만)
        if (!projectMemberRepository.existsById_ProjectIdAndId_UserId(saved.getId(), owner.getId())) {
            ProjectMember pm = new ProjectMember();
            ProjectMemberId pmId = new ProjectMemberId();
            pmId.setProjectId(saved.getId());
            pmId.setUserId(owner.getId());
            pm.setId(pmId);
            pm.setProject(saved);
            pm.setUser(owner);
            pm.setRole(OrganizationRole.MEMBER); // <- 이 줄 필수
            projectMemberRepository.save(pm);
        }

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
    public ProjectMember addMember(Long projectId, Long userId, String actorName) {
        Project project = get(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        // 장기 휴가자는 등록 불가 (정책)
        if (user.getWorkStatus() == User.WorkStatus.LONG_LEAVE) {
            throw new IllegalArgumentException("장기 휴가 중인 사용자는 프로젝트에 추가할 수 없습니다: " + user.getName());
        }

        ProjectMember pm = new ProjectMember();
        ProjectMemberId id = new ProjectMemberId();
        id.setProjectId(project.getId());
        id.setUserId(user.getId());
        pm.setId(id);
        pm.setProject(project);
        pm.setUser(user);
        pm.setRole(OrganizationRole.MEMBER);

        ProjectMember saved = projectMemberRepository.save(pm);
        publisher.publishEvent(new ProjectMemberAddedEvent(projectId, project.getName(), actorName, user.getId()));
        return saved;
    }

    @Override
    public void removeMember(Long projectId, Long userId, Long removedByUserId) {
        ProjectMemberId id = new ProjectMemberId();
        id.setProjectId(projectId);
        id.setUserId(userId);
        projectMemberRepository.deleteById(id);

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
