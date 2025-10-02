
package com.azure.controller.project;

import com.azure.model.project.Project;
import com.azure.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/projects/{id}/tasks")
    public String projectTab(@PathVariable Long id, Model model) {
        Project project = projectService.get(id);
        model.addAttribute("project", project); // 필요하면 그대로 둬
        model.addAttribute("body", "project-tab.jsp");
        model.addAttribute("projectId", id);
        model.addAttribute("projectName", project.getName());
        model.addAttribute("activePage", "project" + id);
        return "mainbar";
    }


}
