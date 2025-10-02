package com.azure.controller;

import com.azure.dto.ProjectProposalDTO;
import com.azure.model.project.ProjectProposal;
import com.azure.service.ProjectProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/project-plan")
@RequiredArgsConstructor
public class PlanRestController {

    private final ProjectProposalService proposalService;

    @PostMapping
    public ProjectProposalDTO createProposal(
            @RequestParam Long proposerId,
            @RequestParam Long organizationId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate
    ) {
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
}

