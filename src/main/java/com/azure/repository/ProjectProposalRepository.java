package com.azure.repository;

import com.azure.model.project.ProjectProposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectProposalRepository extends JpaRepository<ProjectProposal, Long> {
    Page<ProjectProposal> findByOrganizationId(Long organizationId, Pageable pageable);
}
