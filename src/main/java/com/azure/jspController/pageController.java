package com.azure.jspController;

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

import java.util.List;

@Controller
@RequiredArgsConstructor
public class pageController {

    private final UserService userService;
    private final TaskService taskService;

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

    @GetMapping("/meeting")
    public String meeting(Model model) {
        model.addAttribute("body", "meeting.jsp");
        model.addAttribute("activePage", "meeting");
        return "mainbar";
    }

    @GetMapping("/calendar")
    public String calendar(Model model) {
        model.addAttribute("body", "my-calendar.jsp");
        model.addAttribute("activePage", "calendar");
        return "mainbar";
    }

    @GetMapping("/tasks/my")
    public String tasks(@ModelAttribute("user") User me, Model model, Pageable pageable) {
        // WebUserAdvice가 넣어준 user를 그대로 받는다.
        if (me == null) return "redirect:/login";

        Page<Task> page = taskService.listTasksByAssignee(me.getId(), pageable);

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
        return t != null
                && t.getWorkflow() != null
                && Boolean.TRUE.equals(t.getWorkflow().getIsTerminal());
    }
}
