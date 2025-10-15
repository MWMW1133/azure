package com.azure.service.impl;

import com.azure.dto.ProjectProposalDTO;
import com.azure.event.ProposalCreatedEvent;
import com.azure.event.ProposalStatusChangedEvent;
import com.azure.model.Organization;
import com.azure.model.project.Project;
import com.azure.model.project.ProjectProposal;
import com.azure.model.user.User;
import com.azure.repository.OrganizationRepository;
import com.azure.repository.ProjectProposalRepository;
import com.azure.repository.ProjectRepository;
import com.azure.repository.UserRepository;
import com.azure.service.ProjectProposalService;
import com.azure.service.ProjectService;
import com.azure.service.exception.NotFoundException;
import com.azure.model.notify.NotificationType;
import com.azure.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectProposalServiceImpl implements ProjectProposalService {

    private final ProjectProposalRepository proposalRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final ApplicationEventPublisher publisher;

    // ✅ 인터페이스로 주입 (권장)
    private final ProjectService projectService;

    // ✅ 바로 알림 전송용
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    private ProjectProposalDTO toDto(ProjectProposal entity) {
        ProjectProposalDTO dto = new ProjectProposalDTO();
        dto.setId(entity.getId());
        dto.setProposerId(entity.getProposer().getId());
        dto.setProposerName(entity.getProposer().getName());
        dto.setProposerAvatarUrl(entity.getProposer().getAvatarUrl());
        dto.setOrganizationId(entity.getOrganization().getId());
        dto.setProjectId(entity.getProject() != null ? entity.getProject().getId() : null);
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus().name());
        dto.setStartDate(entity.getStartDate());
        dto.setDueDate(entity.getDueDate());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectProposal get(Long id) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proposal not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectProposal> listByOrganization(Long organizationId, Pageable pageable) {
        return proposalRepository.findByOrganizationId(organizationId, pageable);
    }

    @Override
    public ProjectProposal create(Long proposerId, Long organizationId, String name, String description,
                                  LocalDate startDate, LocalDate dueDate) {
        User proposer = userRepository.findById(proposerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + proposerId));
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found: " + organizationId));

        ProjectProposal proposal = new ProjectProposal();
        proposal.setProposer(proposer);
        proposal.setOrganization(org);
        proposal.setName(name);
        proposal.setDescription(description);
        proposal.setStartDate(startDate);
        proposal.setDueDate(dueDate);
        proposal.setStatus(ProjectProposal.Status.PENDING);

        ProjectProposal saved = proposalRepository.save(proposal);
        publisher.publishEvent(new ProposalCreatedEvent(saved.getId()));
        return saved;
    }

    @Override
    @Transactional
    public Project approve(Long proposalId, Long approverId) {
        ProjectProposal proposal = get(proposalId);

        if (proposal.getStatus() == ProjectProposal.Status.REJECTED) {
            throw new IllegalStateException("이미 거절된 제안입니다.");
        }

        // 멱등 처리
        if (proposal.getStatus() == ProjectProposal.Status.APPROVED && proposal.getProject() != null) {
            String approverName = userRepository.findById(approverId).map(User::getName).orElse("SYSTEM");
            Project existing = proposal.getProject();
            ensureProposerMembership(existing.getId(), proposal.getProposer().getId(), approverName);
            return existing;
        }

        // 1) 프로젝트 생성
        Project project = projectService.create(
                proposal.getOrganization().getId(),
                approverId,
                proposal.getName(),
                proposal.getDescription()
        );

        // 1-1) 기간 반영(옵션)
        LocalDate s = proposal.getStartDate();
        LocalDate d = proposal.getDueDate();
        if (s != null || d != null) {
            projectRepository.updateDates(project.getId(), s, d);
        }

        // 2) 상태/연결 갱신 후 저장
        proposal.setStatus(ProjectProposal.Status.APPROVED);
        proposal.setProject(project);
        ProjectProposal saved = proposalRepository.save(proposal);

        // 3) 제안자 멤버십 보정(actorName = 승인자 이름)
        String approverName = userRepository.findById(approverId).map(User::getName).orElse("SYSTEM");
        ensureProposerMembership(project.getId(), proposal.getProposer().getId(), approverName);

        // 4) 알림(직접 전송 + 이벤트 발행)
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("proposalId", saved.getId());
            payload.put("projectId", project.getId());
            payload.put("organizationId", saved.getOrganization().getId());
            payload.put("status", saved.getStatus().name());
            payload.put("name", saved.getName());
            payload.put("message", "‘" + saved.getName() + "’ 제안이 승인되었습니다.");

            notificationService.notifyUser(
                    saved.getProposer().getId(),
                    NotificationType.PROPOSAL_STATUS_CHANGED.name(),
                    objectMapper.writeValueAsString(payload)
            );
        } catch (Exception ignore) {
            notificationService.notifyUser(
                    saved.getProposer().getId(),
                    NotificationType.PROPOSAL_STATUS_CHANGED.name(),
                    "‘" + saved.getName() + "’ 제안이 승인되었습니다."
            );
        }

        publisher.publishEvent(new ProposalStatusChangedEvent(saved.getId(), saved.getStatus().name()));
        return project;
    }

    private void ensureProposerMembership(Long projectId, Long proposerUserId, String actorName) {
        if (!projectService.existsMember(projectId, proposerUserId)) {
            projectService.addMember(projectId, proposerUserId, actorName);
        }
    }

    @Override
    public ProjectProposal reject(Long proposalId, Long approverId) {
        ProjectProposal proposal = get(proposalId);
        if (proposal.getStatus() != ProjectProposal.Status.PENDING) {
            throw new IllegalStateException("Proposal is not in PENDING state");
        }

        proposal.setStatus(ProjectProposal.Status.REJECTED);
        ProjectProposal saved = proposalRepository.save(proposal);

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("proposalId", saved.getId());
            payload.put("projectId", null);
            payload.put("organizationId", saved.getOrganization().getId());
            payload.put("status", saved.getStatus().name());
            payload.put("name", saved.getName());
            payload.put("message", "‘" + saved.getName() + "’ 제안이 거절되었습니다.");

            notificationService.notifyUser(
                    saved.getProposer().getId(),
                    NotificationType.PROPOSAL_STATUS_CHANGED.name(),
                    objectMapper.writeValueAsString(payload)
            );
        } catch (Exception ignore) {
            notificationService.notifyUser(
                    saved.getProposer().getId(),
                    NotificationType.PROPOSAL_STATUS_CHANGED.name(),
                    "‘" + saved.getName() + "’ 제안이 거절되었습니다."
            );
        }

        publisher.publishEvent(new ProposalStatusChangedEvent(saved.getId(), saved.getStatus().name()));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectProposalDTO> listByOrganizationAndStatus(Long organizationId, ProjectProposal.Status status, Pageable pageable) {
        return proposalRepository.findByOrganizationIdAndStatus(organizationId, status, pageable)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectProposalDTO> findByProposerId(Long proposerId) {
        return proposalRepository.findByProposerId(proposerId)
                .stream()
                .map(this::toDto)
                .toList();
    }
}
