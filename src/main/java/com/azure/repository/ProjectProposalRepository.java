package com.azure.repository;

import com.azure.model.project.ProjectProposal;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectProposalRepository extends JpaRepository<ProjectProposal, Long> {
    Page<ProjectProposal> findByOrganizationId(Long organizationId, Pageable pageable);

    @Query("select p.proposer.id from ProjectProposal p where p.id = :proposalId")
    Long getProposerId(Long proposalId);

    @Query("select p.organization.id from ProjectProposal p where p.id = :proposalId")
    Long getOrganizationId(Long proposalId);
    Page<ProjectProposal> findByStatus(ProjectProposal.Status status, Pageable pageable);
    List<ProjectProposal> findByOrganizationIdAndStatus(Long organizationId, ProjectProposal.Status status);

}
