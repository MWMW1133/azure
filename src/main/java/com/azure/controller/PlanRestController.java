package com.azure.controller;

import com.azure.dto.ProjectProposalDTO;
import com.azure.model.project.ProjectProposal;
import com.azure.service.ProjectProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import static com.azure.security.SecurityUtil.getCurrentUserId;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/project-plan")
@RequiredArgsConstructor
public class PlanRestController {

    private final ProjectProposalService proposalService;

    @PostMapping
    public ProjectProposalDTO createProposal(
            @ModelAttribute("currentUserId") Long proposerId,
            @RequestParam Long organizationId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate
        ) {
        if (proposerId == null) throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

        ProjectProposal entity = proposalService.create(
                proposerId, organizationId, name, description, startDate, dueDate);

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
    @PutMapping("/{proposalId}/status")
    public ProjectProposalDTO updateStatus(@PathVariable Long proposalId,
                                           @RequestParam String status,
                                           @ModelAttribute("currentUserId") Long meId) {

    if (meId == null) throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
            
    ProjectProposal entity;
    if ("APPROVED".equalsIgnoreCase(status)) {
        proposalService.approve(proposalId, getCurrentUserId());
    } else if ("REJECTED".equalsIgnoreCase(status)) {
        entity = proposalService.reject(proposalId, getCurrentUserId());
    } else {
        throw new IllegalArgumentException("Unknown status: " + status);
    }

    // entity 최신화
    entity = proposalService.get(proposalId);

    // DTO 변환
    ProjectProposalDTO dto = new ProjectProposalDTO();
    dto.setId(entity.getId());
    dto.setStatus(entity.getStatus().name());
    dto.setName(entity.getName());
    dto.setDescription(entity.getDescription());
    return dto;
}
}

