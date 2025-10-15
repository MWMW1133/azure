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
    private final ProjectServiceImpl projectService;

    // ✅ 바로 알림 전송용
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    // 🔹 엔티티 → DTO 변환 메서드
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

    // 제안 단건 조회
    @Override
    @Transactional(readOnly = true)
    public ProjectProposal get(Long id) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proposal not found: " + id));
    }

    // 조직별 제안 목록 조회 (DTO 변환 없음 → 필요 시 컨트롤러에서 toDto)
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectProposal> listByOrganization(Long organizationId, Pageable pageable) {
        return proposalRepository.findByOrganizationId(organizationId, pageable);
    }

    // 제안 생성
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

        // 📢 이벤트 발행(관리자 등에게 신규 제안 알림 리스너에서 처리 가능)
        publisher.publishEvent(new ProposalCreatedEvent(saved.getId()));

        return saved;
    }

    // 제안 승인
    @Override
    @Transactional
    public Project approve(Long proposalId, Long approverId) {
        ProjectProposal proposal = get(proposalId);

        if (proposal.getStatus() == ProjectProposal.Status.REJECTED) {
            throw new IllegalStateException("이미 거절된 제안입니다.");
        }

        // 이미 프로젝트가 연결된 승인 상태면 그대로 반환 (멱등 처리)
        if (proposal.getStatus() == ProjectProposal.Status.APPROVED && proposal.getProject() != null) {
            Project existing = proposal.getProject();
            ensureProposerMembership(existing.getId(), proposal.getProposer().getId());
            return existing;
        }

        // 1) 프로젝트 생성 (owner는 approver로 설정)
        Project project = projectService.create(
                proposal.getOrganization().getId(),
                approverId,
                proposal.getName(),
                proposal.getDescription()
        );

        // 옵션: 제안서 기간을 프로젝트 기간에 반영
        LocalDate s = proposal.getStartDate();
        LocalDate d = proposal.getDueDate();
        if (s != null || d != null) {
            projectRepository.updateDates(project.getId(), s, d);
        }

        // 2) 제안 상태/연결 갱신 후 저장
        proposal.setStatus(ProjectProposal.Status.APPROVED);
        proposal.setProject(project);
        ProjectProposal saved = proposalRepository.save(proposal);

        // 3) 제안자 → 프로젝트 멤버 자동 추가 (중복 방지)
        ensureProposerMembership(project.getId(), proposal.getProposer().getId());

        // ✅ 바로 알림(제안자에게) — 프론트에서 딥링크/메시지 구성에 쓰는 키 포함
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
            // JSON 직렬화 실패 시에도 최소 문자열로 발사
            notificationService.notifyUser(
                    saved.getProposer().getId(),
                    NotificationType.PROPOSAL_STATUS_CHANGED.name(),
                    "‘" + saved.getName() + "’ 제안이 승인되었습니다."
            );
        }

        // 📢 이벤트도 발행(리스너가 AFTER_COMMIT에서 보조 동작 가능)
        publisher.publishEvent(new ProposalStatusChangedEvent(saved.getId(), saved.getStatus().name()));

        return project;
    }

    private void ensureProposerMembership(Long projectId, Long proposerUserId) {
        if (!projectService.existsMember(projectId, proposerUserId)) {
            projectService.addMember(projectId, proposerUserId);
        }
    }

    // 제안 거절
    @Override
    public ProjectProposal reject(Long proposalId, Long approverId) {
        ProjectProposal proposal = get(proposalId);

        if (proposal.getStatus() != ProjectProposal.Status.PENDING) {
            throw new IllegalStateException("Proposal is not in PENDING state");
        }

        proposal.setStatus(ProjectProposal.Status.REJECTED);
        ProjectProposal saved = proposalRepository.save(proposal);

        // ✅ 바로 알림(제안자에게)
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

        // 📢 이벤트 발행
        publisher.publishEvent(new ProposalStatusChangedEvent(saved.getId(), saved.getStatus().name()));

        return saved;
    }

    // 조직 + 상태별 제안 목록 조회 → DTO 변환
    @Override
    @Transactional(readOnly = true)
    public List<ProjectProposalDTO> listByOrganizationAndStatus(Long organizationId, ProjectProposal.Status status, Pageable pageable) {
        return proposalRepository.findByOrganizationIdAndStatus(organizationId, status, pageable)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // 제안자별 제안 목록 조회 → DTO 변환
    @Override
    @Transactional(readOnly = true)
    public List<ProjectProposalDTO> findByProposerId(Long proposerId) {
        return proposalRepository.findByProposerId(proposerId)
                .stream()
                .map(this::toDto)
                .toList();
    }
}
