package com.azure.jspController;

import java.util.ArrayList;
import java.util.List;

import com.azure.model.task.Task;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    // 진입점 추가
    @GetMapping
    public String redirectTasks() {
        return "redirect:/tasks/my";
    }

    @GetMapping("/my")
    public String getMyTasks(Model model) {

        List<Task> activeTasks = new ArrayList<>();
        List<Task> archivedTasks = new ArrayList<>();

        //////////////////더머ㅣ데이터//////////////
        // 상위 태스크
//        Task parent = new Task(
//                100L, "프론트엔드 UI 작업", "김수빈", "user2.png",
//                "2025-10-08","2025-10-10", "Assignments", "high", 40, true, "2025-09-28"
//        );
//
//        // 하위 태스크들
//        Task sub1 = new Task(
//                101L, "사이드바 스타일링", "이재환", "user1.png",
//                "2025-10-01","2025-10-05", "in-progress", "normal", 70, false, "2025-09-27"
//        );
//        Task sub2 = new Task(
//                102L, "캘린더 팝업 수정", "박민수", "user3.png",
//                "2025-10-01","2025-10-06", "Reviewing", "low", 0, false, "2025-09-26"
//        );
//
//        parent.addSubTask(sub1);
//        parent.addSubTask(sub2);
//
//        activeTasks.add(parent);
//
//        // 완료된 태스크 더미
//        Task archived = new Task(
//                200L, "DB 스키마 설계", "최은지", "user4.png","2025-09-02",
//                "2025-09-20", "Completed", "normal", 100, true, "2025-09-22"
//        );
//        archivedTasks.add(archived);

//        model.addAttribute("activeTasks", activeTasks);
//        model.addAttribute("archivedTasks", archivedTasks);

        // =====================
        // 🔹 백엔드 연동
        // List<Task> activeTasks = taskService.findActiveTasks();
        // List<Task> archivedTasks = taskService.findArchivedTasks();
        // =====================

        // ===== 수정 return "my-tasks"; // => /WEB-INF/views/my-tasks.jsp

        model.addAttribute("body", "my-tasks.jsp");
        model.addAttribute("activePage", "tasks");

        return "mainbar";
    }
}