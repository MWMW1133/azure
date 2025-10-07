package com.azure.controller;

import com.azure.service.ProjectService;
import com.azure.service.TaskService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;



@Controller
@RequestMapping("/projects/{projectId}")
@RequiredArgsConstructor
public class ProjectPageController {
    private final ProjectService projectService;

    @GetMapping
    public String projectMain(@PathVariable Long projectId, Model model) {
        var p = projectService.get(projectId);
        model.addAttribute("projectId", p.getId());
        model.addAttribute("projectName", p.getName());

        // 사이드바 활성화
        model.addAttribute("activePage", "project");
        model.addAttribute("activeProjectId", projectId);
        model.addAttribute("body", "/WEB-INF/views/project-tab.jsp");

        return "mainbar";
    }

    @GetMapping("/table")
    public String table(@PathVariable Long projectId, Model model) {
        // 살려줘

        return "projects/mainTable";
    }


    @GetMapping("/card")    public String card(@PathVariable Long projectId){ return "projects/fragments/card"; }
    @GetMapping("/gantt")   public String gantt(@PathVariable Long projectId){ return "projects/fragments/gantt"; }
    @GetMapping("/chart")   public String chart(@PathVariable Long projectId){ return "projects/fragments/chart"; }
    @GetMapping("/calendar")public String calendar(@PathVariable Long projectId){ return "projects/fragments/calendar"; }
    @GetMapping("/files")   public String files(@PathVariable Long projectId){ return "projects/fragments/files"; }
    @GetMapping("/members") public String members(@PathVariable Long projectId){ return "projects/fragments/members"; }

}
