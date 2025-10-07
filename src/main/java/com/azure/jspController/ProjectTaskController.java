//package com.azure.jspController;
//
//import com.azure.model.task.Task;
//import com.azure.service.TaskService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//public class ProjectTaskController {
//
//    private final TaskService taskService;
//
//    /**
//     * 프로젝트 메인 탭 (프로젝트 대시보드 등)
//     */
//    @GetMapping("/projects")
//    public String projectList() {
//        return "project-tab";  // /WEB-INF/views/project-tab.jsp
//    }
//
//    /**
//     * 프로젝트별 메인 테이블
//     * ex) /projects/1/table
//     */
//    @GetMapping("/projects/{projectId}/table")
//    public String projectTable(@PathVariable Long projectId, Model model) {
//
//        // === 실제 DB에서 태스크 목록 불러오기 ===
//        List<Task> activeTasks =
//                taskService.listByProject(projectId, Pageable.unpaged()).getContent();
//
//        List<Task> archivedTasks =
//                taskService.listCompletedTasksByProject(projectId, Pageable.unpaged()).getContent();
//
//        // === JSP에서 사용할 데이터 모델 ===
//        model.addAttribute("projectId", projectId);
//        model.addAttribute("activeTasks", activeTasks);
//        model.addAttribute("archivedTasks", archivedTasks);
//
//        // === projects/mainTable.jsp 렌더링 ===
//        return "projects/mainTable";
//    }
//}
