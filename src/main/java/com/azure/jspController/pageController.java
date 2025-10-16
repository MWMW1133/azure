package com.azure.jspController;

import com.azure.config.WebUserAdvice;
import com.azure.model.task.Task;
import com.azure.model.user.User;
import com.azure.service.TaskService;
import com.azure.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class pageController {

    private final UserService userService;
    private final TaskService taskService;
    private final WebUserAdvice webUserAdvice;

    @GetMapping("/")
    public String defaultPage() {
        return "redirect:/login";
    }

    @GetMapping("/profile")
        public String viewProfile(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        model.addAttribute("activePage", "profile");
        model.addAttribute("body", "viewProfile.jsp");
        return "mainbar";
    }


    @GetMapping("/noInvitePage")
    public String noInvitePage() {
        return "noInvitePage"; // /WEB-INF/views/noInvitePage.jsp
    }



    @GetMapping("/calendar")
    public String calendar(Model model) {
        model.addAttribute("body", "my-calendar.jsp");
        model.addAttribute("activePage", "calendar");
        return "mainbar";
    }


    @GetMapping("/tasks/my")
    public String tasks(Model model, Pageable pageable, HttpSession session) {
        Long me = webUserAdvice.currentUserId(session);
        Page<Task> page = taskService.listTasksByAssignee(me, pageable);

        List<Task> active = page.getContent().stream()
            .filter(t -> !isTerminal(t))
            .toList();

        List<Task> archived = page.getContent().stream()
            .filter(this::isTerminal)
            .toList();

        model.addAttribute("activeTasks", active);
        model.addAttribute("archivedTasks", archived);
        model.addAttribute("body", "my-tasks.jsp");
        model.addAttribute("activePage", "tasks");
        return "mainbar";
    }

    private boolean isTerminal(Task t) {
        return t.getWorkflow() != null
            && Boolean.TRUE.equals(t.getWorkflow().getIsTerminal());
    }


    @Controller
    public class ModalController {
        // 직접 접근인가? 아니면 include해서 해결 안되나?
        @GetMapping("/event-modal")
        public String eventModal() {
            return "my-calendar-modal"; // /WEB-INF/views/my-calendar-modal.jsp
        }

        @GetMapping("/plan")
        public String plan() {

            return "project-plan"; // /WEB-INF/views/project-plan.jsp
        }

    }
    
}

