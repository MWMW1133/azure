package com.azure.service.impl;

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
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectProposalServiceImpl implements ProjectProposalService {

    private final ProjectProposalRepository proposalRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final ApplicationEventPublisher publisher; // 📢 이벤트 퍼블리셔 추가

    // 제안 단건 조회
    @Override
    @Transactional(readOnly = true)
    public ProjectProposal get(Long id) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proposal not found: " + id));
    }
    // 조직별 제안 목록 조회
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

        // 📢 이벤트 발행 (관리자에게 새 제안 생성 알림)
        publisher.publishEvent(new ProposalCreatedEvent(saved.getId()));

        return saved;
    }
    // 제안 승인
    @Override
    public Project approve(Long proposalId, Long approverId) {
        ProjectProposal proposal = get(proposalId);

        if (proposal.getStatus() != ProjectProposal.Status.PENDING) {
            throw new IllegalStateException("Proposal is not in PENDING state");
        }

        proposal.setStatus(ProjectProposal.Status.APPROVED);

        // 프로젝트 생성
        Project project = new Project();
        project.setName(proposal.getName());
        project.setDescription(proposal.getDescription());
        project.setOwner(proposal.getProposer()); // 제안자를 프로젝트 소유자로 설정
        project.setOrganization(proposal.getOrganization());
        project.setStartDate(proposal.getStartDate());
        project.setDueDate(proposal.getDueDate());

        Project savedProject = projectRepository.save(project);

        // proposal과 project 연결
        proposal.setProject(savedProject);
        proposalRepository.save(proposal);
        
        // 📢 이벤트 발행 (제안자에게 승인 알림)
        publisher.publishEvent(new ProposalStatusChangedEvent(proposal.getId(), proposal.getStatus().name()));

        return savedProject;
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

        // 📢 이벤트 발행 (제안자에게 거절 알림)
        publisher.publishEvent(new ProposalStatusChangedEvent(saved.getId(), saved.getStatus().name()));

        return saved;
    }
    // 조직과 상태로 제안 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectProposal> listByOrganizationAndStatus(Long organizationId, ProjectProposal.Status status, Pageable pageable) {
        return proposalRepository.findByOrganizationIdAndStatus(organizationId, status, pageable);
    }
    // 제안자 ID로 제안 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<ProjectProposal> findByProposerId(Long proposerId) {
        return proposalRepository.findByProposerId(proposerId);
    }

}
