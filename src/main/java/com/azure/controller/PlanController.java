package com.azure.controller;

import com.azure.dto.ProjectProposalDTO;
import com.azure.model.project.ProjectProposal;
import com.azure.service.ProjectProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PlanController {

    private final ProjectProposalService proposalService;

    @GetMapping("/project-plan")
    public String showProjectPlanPage(Model model) {
        Long organizationId = 1L; // TODO: 세션에서 가져오기
        var pageable = PageRequest.of(0, 50);

        // ✅ ServiceImpl에서 DTO 변환 끝낸 데이터 가져오기
        List<ProjectProposalDTO> newPlans = proposalService.listByOrganizationAndStatus(organizationId, ProjectProposal.Status.PENDING, pageable);
        List<ProjectProposalDTO> approvedPlans = proposalService.listByOrganizationAndStatus(organizationId, ProjectProposal.Status.APPROVED, pageable);
        List<ProjectProposalDTO> rejectedPlans = proposalService.listByOrganizationAndStatus(organizationId, ProjectProposal.Status.REJECTED, pageable);

        model.addAttribute("newPlans", newPlans);
        model.addAttribute("approvedPlans", approvedPlans);
        model.addAttribute("rejectedPlans", rejectedPlans);

        model.addAttribute("body", "project-plan.jsp");
        model.addAttribute("activePage", "plan");
        return "mainbar";
    }
}
