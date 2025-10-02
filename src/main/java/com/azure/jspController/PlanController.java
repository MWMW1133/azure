package com.azure.jspController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.azure.dto.ProjectProposalDTO;
import com.azure.service.ProjectProposalService;

import java.util.List;

@Controller
public class PlanController {

    private final ProjectProposalService proposalService;

    @Autowired
    public PlanController(ProjectProposalService proposalService) {
        this.proposalService = proposalService;
    }

    // 이 주소로 접속하면 project-plan.jsp 화면이 보입니다.
    @GetMapping("/project-plan")
    public String showProjectPlanPage(Model model) {

//        // Service를 통해 상태별 데이터를 각각 가져옵니다.
//        List<ProjectProposalDTO> newPlans = proposalService.getNewProposals();
//        List<ProjectProposalDTO> approvedPlans = proposalService.getApprovedProposals();
//        List<ProjectProposalDTO> rejectedPlans = proposalService.getRejectedProposals();
//
//        // JSP에서 사용할 이름(${newPlans} 등)으로 Model에 담습니다.
//        model.addAttribute("newPlans", newPlans);
//        model.addAttribute("approvedPlans", approvedPlans);
//        model.addAttribute("rejectedPlans", rejectedPlans);

        // 수정
        model.addAttribute("body", "project-plan.jsp");
        model.addAttribute("activePage", "plan");   // sidebar에서 비교용 key
        return "mainbar";
    }
}