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
import com.azure.repository.TagRepository;
import com.azure.repository.TaskRepository;
import com.azure.repository.UserRepository;
import com.azure.repository.WorkflowRepository;
import com.azure.service.ProjectService;
import com.azure.service.WorkflowService;
import com.azure.service.exception.NotFoundException;

import jakarta.persistence.EntityManager;
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
    private final WorkflowRepository workflowRepository; // 🔹 추가
    // ✅ 중복 제거: memberRepository 필드 삭제
    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;

    // ✅ 워크플로우 생성 책임은 전담 서비스에 위임
    private final WorkflowService workflowService;

    // 📢 프로젝트 멤버 추가/삭제 이벤트 발행
    private final ApplicationEventPublisher publisher;
    private EntityManager em; // 🔹 flush/clear용

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

        // ✅ 기본 워크플로우 보장
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
            pm.setRole(OrganizationRole.MEMBER);
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
    @Transactional
    public void delete(Long projectId) {
        // 존재 확인
        projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("project not found: " + projectId));

        // 1) self-FK 끊기 → 태스크 삭제
        taskRepository.detachParentsByProjectId(projectId);
        taskRepository.deleteByProjectId(projectId);

        // 2) 태그 삭제
        tagRepository.deleteByProjectId(projectId);

        // 3) 멤버 삭제
        projectMemberRepository.deleteByProjectId(projectId);

        // 4) 워크플로우 삭제(프로젝트 기본 단계 등)
        workflowRepository.deleteByProjectId(projectId);

        // 🔸 벌크쿼리 이후 1차 캐시와 DB 상태 동기화
        em.flush();
        em.clear();

        // 5) 마지막에 프로젝트 자체 삭제 (엔티티 메소드 대신 벌크쿼리 사용)
        projectRepository.deleteByIdHard(projectId);
    }

    @Override
    public ProjectMember addMember(Long projectId, Long userId, String actorName) {
        Project project = get(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        // 정책: 장기 휴가자는 등록 불가
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
