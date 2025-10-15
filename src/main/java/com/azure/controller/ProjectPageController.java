package com.azure.controller;

import com.azure.config.WebUserAdvice;
import com.azure.dto.ProjectMemberDTO;
import com.azure.model.project.Project;
import com.azure.model.project.ProjectMember;
import com.azure.model.task.Task;
import com.azure.service.ProjectService;
import com.azure.service.TaskService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}")
@RequiredArgsConstructor
public class ProjectPageController {

    private final ProjectService projectService;
    private final TaskService taskService;
    private final WebUserAdvice webUserAdvice;

    /** 프로젝트 메인 페이지 (사이드바 + 탭 진입) */
    @GetMapping
    public String projectMain(@PathVariable Long projectId, Model model) {
        Project p = projectService.get(projectId);
        model.addAttribute("projectId", p.getId());
        model.addAttribute("projectName", p.getName());
        model.addAttribute("activePage", "project");
        model.addAttribute("activeProjectId", projectId);
        // mainbar.jsp가 <jsp:include page="${body}"/> 라면 파일명만
        model.addAttribute("body", "project-tab.jsp");
        return "mainbar";
    }

    /** 간트 차트 탭 */
    @GetMapping("/gantt")
    public String gantt(@PathVariable Long projectId, Model model) {
        Project p = projectService.get(projectId);

        // 진행중 태스크 (List)
        List<Task> activeTasks = taskService.listByProject(projectId);

        // 완료(아카이브) 태스크 (Page → List)
        Page<Task> archivedPage = taskService.listCompletedTasksByProject(projectId, Pageable.unpaged());
        List<Task> archivedTasks = (archivedPage != null) ? archivedPage.getContent() : java.util.List.of();

        model.addAttribute("projectId", projectId);
        model.addAttribute("projectName", p.getName());
        model.addAttribute("activeTasks", activeTasks);
        model.addAttribute("archivedTasks", archivedTasks);

        return "projects/fragments/ganttTab";
    }

    /** 차트 탭 */
    @GetMapping("/chart")
    public String chart(@PathVariable Long projectId, Model model) {
        List<Task> all = taskService.getTasksForProject(projectId);  // 하위 포함
        long total = all.size();

        long completed = all.stream()
                .filter(t -> t.getWorkflow() != null && Boolean.TRUE.equals(t.getWorkflow().getIsTerminal()))
                .count();

        if (completed == 0) {
            completed = taskService
                    .listCompletedTasksByProject(projectId, Pageable.unpaged())
                    .getTotalElements();
        }

        long active = Math.max(0, total - completed);

        model.addAttribute("projectId", projectId);
        model.addAttribute("totalTaskCount", total);
        model.addAttribute("activeTaskCount", active);
        model.addAttribute("archivedTaskCount", completed);
        return "projects/fragments/chartTab";
    }

    /** 캘린더 탭 */
    @GetMapping("/calendar")
    public String calendar(@PathVariable Long projectId, Model model) {
        model.addAttribute("projectId", projectId);
        return "projects/project-calendar";
    }

    /** 파일 탭 */
    @GetMapping("/files")
    public String files(@PathVariable Long projectId) {
        return "projects/files";
    }

    /** 관리 탭 */
    @GetMapping("/management")
    public String management(@PathVariable Long projectId, Model model) {
        Project p = projectService.get(projectId);
        model.addAttribute("projectId", p.getId());
        model.addAttribute("projectName", p.getName());
        model.addAttribute("activePage", "project");
        model.addAttribute("activeProjectId", projectId);
        return "projects/fragments/managementTab";
    }

    /** 메인 테이블 탭 */
    @GetMapping("/table")
    public String table(@PathVariable Long projectId, Model model) {
        Project project = projectService.get(projectId);

        // 진행중 태스크 (List)
        List<Task> activeTasks = taskService.listByProject(projectId);

        // 완료(아카이브) 태스크 (Page → List)
        Page<Task> archivedPage = taskService.listCompletedTasksByProject(projectId, Pageable.unpaged());
        List<Task> archivedTasks = (archivedPage != null) ? archivedPage.getContent() : java.util.List.of();

        model.addAttribute("projectId", projectId);
        model.addAttribute("projectName", project.getName());
        model.addAttribute("activeTasks", activeTasks);
        model.addAttribute("archivedTasks", archivedTasks);

        return "projects/mainTable";
    }

    //프로젝트 멤버 리스트 가져오기
    @GetMapping("/members/list")
    @ResponseBody
    public Page<ProjectMemberDTO> listMembers(@PathVariable Long projectId, Pageable pageable) {
        return projectService.listMembers(projectId, pageable).map(pm -> {
            var u = pm.getUser();
            var dto = new ProjectMemberDTO();
            dto.setProjectId(projectId);
            dto.setUserId(u != null ? u.getId() : null);
            dto.setUserName(u != null ? u.getName() : null);
            dto.setUserAvatarUrl(u != null ? u.getAvatarUrl() : null);
            return dto;
        });
    }

    //프로젝트 멤버 삭제
    @DeleteMapping("/members/{userId}")
    @ResponseBody
    public ResponseEntity<Void> removeMember(@PathVariable Long projectId, @PathVariable Long userId, HttpSession session) {
      Long me = webUserAdvice.currentUserId(session);

        projectService.removeMember(projectId, userId, me);
        return ResponseEntity.noContent().build(); 
    }

    //프로젝트 삭제
    @DeleteMapping
    @ResponseBody
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.delete(projectId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); 
    }
}
