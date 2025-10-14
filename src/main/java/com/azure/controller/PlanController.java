package com.azure.controller;

import com.azure.model.project.ProjectProposal;
import com.azure.repository.OrganizationMemberRepository;
import com.azure.service.ProjectProposalService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class PlanController {

    private final ProjectProposalService proposalService;
    private final OrganizationMemberRepository organizationMemberRepository;

    @GetMapping("/project-plan")
    public String showProjectPlanPage(Model model,
                                      @ModelAttribute("currentUserId") Long uid,
                                      HttpSession session) {

        if (uid == null) return "redirect:/login";

        Long organizationId = null;
        var firstMember = organizationMemberRepository
                .findByUserIdFetchOrganization(uid)
                .stream()
                .findFirst()
                .orElse(null);
        if (firstMember != null && firstMember.getOrganization() != null) {
            organizationId = firstMember.getOrganization().getId();
        }

        // ✅ 프런트에서 읽을 수 있도록 명시적으로 내려준다
        model.addAttribute("currentUserId", uid);
        model.addAttribute("organizationId", organizationId);

        var pageable = PageRequest.of(0, 50);
        var newPlans      = proposalService.listByOrganizationAndStatus(organizationId, ProjectProposal.Status.PENDING,   pageable);
        var approvedPlans = proposalService.listByOrganizationAndStatus(organizationId, ProjectProposal.Status.APPROVED, pageable);
        var rejectedPlans = proposalService.listByOrganizationAndStatus(organizationId, ProjectProposal.Status.REJECTED, pageable);

        model.addAttribute("newPlans", newPlans);
        model.addAttribute("approvedPlans", approvedPlans);
        model.addAttribute("rejectedPlans", rejectedPlans);
        model.addAttribute("body", "project-plan.jsp");
        model.addAttribute("activePage", "plan");
        return "mainbar";
    }
}
