package com.azure.service;

import com.azure.model.project.ProjectProposal;
import com.azure.model.project.Project;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectProposalService {

    /** 특정 제안 단건 조회 */
    ProjectProposal get(Long id);

    /** 조직별 제안 목록 조회 */
    Page<ProjectProposal> listByOrganization(Long organizationId, Pageable pageable);

    /** 제안 생성 */
    ProjectProposal create(Long proposerId, Long organizationId, String name, String description,
                           java.time.LocalDate startDate, java.time.LocalDate dueDate);

    /** 제안 승인 -> 프로젝트 자동 생성 */
    Project approve(Long proposalId, Long approverId);

    /** 제안 거절 */
    ProjectProposal reject(Long proposalId, Long approverId);
    /** 조직 + 상태별 제안 목록 조회 */
    Page<ProjectProposal> listByOrganizationAndStatus(Long organizationId, ProjectProposal.Status status, Pageable pageable);
    
    /** 제안자별 제안 목록 조회 */
    List<ProjectProposal> findByProposerId(Long proposerId);
}
