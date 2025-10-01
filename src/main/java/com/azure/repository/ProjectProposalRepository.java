package com.azure.repository;

import com.azure.model.project.ProjectProposal;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectProposalRepository extends JpaRepository<ProjectProposal, Long> {
    // 조직별 제안 목록 조회
    Page<ProjectProposal> findByOrganizationId(Long organizationId, Pageable pageable);
    // 제안자 ID 조회
    @Query("select p.proposer.id from ProjectProposal p where p.id = :proposalId")
    Long getProposerId(Long proposalId);
    // 조직 ID 조회
    @Query("select p.organization.id from ProjectProposal p where p.id = :proposalId")
    Long getOrganizationId(Long proposalId);

        // 🔹 상태별 조회
    Page<ProjectProposal> findByOrganizationIdAndStatus(Long organizationId, ProjectProposal.Status status, Pageable pageable);

    // 🔹 제안자별 조회
    List<ProjectProposal> findByProposerId(Long proposerId);
}
