package com.azure.controller;

import com.azure.service.ProjectService;
import com.azure.service.TaskService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
@RequestMapping("/projects/{projectId}")
@RequiredArgsConstructor
public class ProjectPageController {

    private final ProjectService projectService;
    private final TaskService taskService;

    /** 📌 프로젝트 메인 페이지 (사이드바 + 탭 진입) */
      @GetMapping
    public String projectMain(@PathVariable Long projectId, Model model) {
        var p = projectService.get(projectId);
        model.addAttribute("projectId", p.getId());
        model.addAttribute("projectName", p.getName());

        // ✅ 사이드바 키는 'project' 고정 + 어떤 프로젝트인지 별도 제공
        model.addAttribute("activePage", "project");
        model.addAttribute("activeProjectId", projectId);

        // ✅ mainbar.jsp가 <jsp:include page="${body}"/> 라면 파일명만
        model.addAttribute("body", "project-tab.jsp");
        return "mainbar";
    }

    /** 📌 카드 탭 */
    @GetMapping("/card")
    public String card(@PathVariable Long projectId) {
        return "projects/card";
    }

    /** 📌 간트 차트 탭 */
    @GetMapping("/gantt")
    public String gantt(@PathVariable Long projectId, Model model) {
        var p = projectService.get(projectId);

        // 진행중 태스크 (List)
        var activeTasks = taskService.listByProject(projectId);

        // 완료(아카이브) 태스크 (Page → List)
        var archivedPage  = taskService.listCompletedTasksByProject(projectId, Pageable.unpaged());
        var archivedTasks = (archivedPage != null) ? archivedPage.getContent() : java.util.List.of();

        model.addAttribute("projectId", projectId);
        model.addAttribute("projectName", p.getName());
        model.addAttribute("activeTasks", activeTasks);
        model.addAttribute("archivedTasks", archivedTasks); // ✅ 여기!

        return "projects/fragments/ganttTab";
    }

    /** 📌 차트 탭 */
    @GetMapping("/chart")
    public String chart(@PathVariable Long projectId, Model model) {
    var all = taskService.getTasksForProject(projectId);  // 하위 포함

    long total = all.size();

    long completed = all.stream()
            .filter(t -> t.getWorkflow() != null && Boolean.TRUE.equals(t.getWorkflow().getIsTerminal()))
            .count();

    if (completed == 0) {
        completed = taskService
            .listCompletedTasksByProject(projectId, org.springframework.data.domain.Pageable.unpaged())
            .getTotalElements();
    }

    long active = Math.max(0, total - completed);

    model.addAttribute("projectId", projectId);
    model.addAttribute("totalTaskCount", total);
    model.addAttribute("activeTaskCount", active);
    model.addAttribute("archivedTaskCount", completed);
        return "projects/fragments/chartTab";
    }

    /** 📌 캘린더 탭 */
    @GetMapping("/calendar")
    public String calendar(@PathVariable Long projectId, Model model) {
        model.addAttribute("projectId", projectId);
        return "projects/project-calendar";
    }

    /** 📌 파일 탭 */
    @GetMapping("/files")
    public String files(@PathVariable Long projectId) {
        return "projects/files";
    }

    /** 📌 멤버 탭 */
    @GetMapping("/members")
    public String members(@PathVariable Long projectId) {
        return "projects/members";
    }

    @GetMapping("/table")
    public String table(@PathVariable Long projectId, Model model) {
        var project = projectService.get(projectId);

        // 진행중 태스크 (List)
        var activeTasks = taskService.listByProject(projectId);

        // 완료(아카이브) 태스크 (Page → List)
        var archivedPage  = taskService.listCompletedTasksByProject(projectId, Pageable.unpaged());
        var archivedTasks = (archivedPage != null) ? archivedPage.getContent() : java.util.List.of();

        model.addAttribute("projectId", projectId);
        model.addAttribute("projectName", project.getName());
        model.addAttribute("activeTasks", activeTasks);
        model.addAttribute("archivedTasks", archivedTasks); // ✅ 여기!

        return "projects/mainTable";
    }

}