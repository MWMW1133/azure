package com.azure.service.impl;

import com.azure.model.Organization;
import com.azure.model.project.Project;
import com.azure.model.project.ProjectMember;
import com.azure.model.project.ProjectMemberId;
import com.azure.model.user.User;
import com.azure.repository.OrganizationRepository;
import com.azure.repository.ProjectMemberRepository;
import com.azure.repository.ProjectRepository;
import com.azure.repository.UserRepository;
import com.azure.service.ProjectService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 프로젝트 도메인 서비스 구현.
 * - 생성 시 조직/소유자 FK 확인
 * - 멤버십은 조인 테이블(project_members)을 통해 관리
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    @Override @Transactional(readOnly = true)
    public Project get(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found: " + id));
    }

    @Override @Transactional(readOnly = true)
    public Page<Project> list(Pageable pageable) { return projectRepository.findAll(pageable); }

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
        return projectRepository.save(p);
    }

    @Override
    public Project update(Long projectId, String name, String description) {
        Project p = get(projectId);
        if (name != null) p.setName(name);
        if (description != null) p.setDescription(description);
        return projectRepository.save(p);
    }

    @Override
    public void delete(Long projectId) { projectRepository.delete(get(projectId)); }

    @Override
    public ProjectMember addMember(Long projectId, Long userId, String role) {
        Project project = get(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        ProjectMember pm = new ProjectMember();
        pm.setId(new ProjectMemberId());
        pm.getId().setProjectId(project.getId());
        pm.getId().setUserId(user.getId());
        pm.setProject(project);
        pm.setUser(user);
        pm.setRole(role);
        return projectMemberRepository.save(pm);
    }

    @Override
    public void removeMember(Long projectId, Long userId) {
        ProjectMemberId id = new ProjectMemberId();
        id.setProjectId(projectId);
        id.setUserId(userId);
        projectMemberRepository.deleteById(id);
    }
}
