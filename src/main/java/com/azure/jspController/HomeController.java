package com.azure.jspController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.azure.model.enums.PriorityCode;
import com.azure.model.task.Task;
import com.azure.model.user.User;
import com.azure.service.TaskService;
import com.azure.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.*;
import com.azure.dto.TodoItem;
import com.azure.config.WebUserAdvice;
import com.azure.dto.HomeTaskCard; 

@Controller
@RequiredArgsConstructor
public class HomeController {
        private final WebUserAdvice webUserAdvice;
        private final TaskService taskService;

        private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yy-MM-dd");
        private static final int DUE_SOON_DAYS = 3;

        @GetMapping("/home")
        public String home(Model model, HttpSession session) {
                Long userId = webUserAdvice.currentUserId(session);
                
        var pageSize = 50; 
        List<Task> myTasks = taskService
                .listByAssignee(userId, PageRequest.of(0, pageSize))
                .getContent();

        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(DUE_SOON_DAYS);
                
        // 마감일 다되어가는 태스크들
        List<HomeTaskCard> inprogressTasks = myTasks.stream()
                .filter(t -> t.getWorkflow() == null || !Boolean.TRUE.equals(t.getWorkflow().getIsTerminal()))
                .filter(t -> t.getDueDate() != null
                          && !t.getDueDate().isBefore(today)
                          && !t.getDueDate().isAfter(until))
                .sorted(Comparator
                        .comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Task::getId))
                .map(this::toCard)
                .toList();

        // 긴급
        List<HomeTaskCard> doneTasks = myTasks.stream()
                .filter(t -> t.getPriority() != null && t.getPriority().getId() == 5)
                .sorted(Comparator
                        .comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Task::getId))
                .map(this::toCard)
                .toList();


        model.addAttribute("inprogressTasks", inprogressTasks);
        model.addAttribute("doneTasks", doneTasks);


        model.addAttribute("body", "home.jsp");
        model.addAttribute("activePage", "home");

        return "mainbar";
    }
        private static String nz(String s) { return s == null ? "" : s; }
        private HomeTaskCard toCard(Task t) {
        String assigneeName  = t.getAssignee() != null ? nz(t.getAssignee().getName()) : "";
        String assigneeImage = t.getAssignee() != null ? nz(t.getAssignee().getAvatarUrl()) : "";
        String startedAt = t.getStartDate() != null ? t.getStartDate().format(YMD) : "";
        String dueDate   = t.getDueDate()   != null ? t.getDueDate().format(YMD)   : "";
        String workflow = t.getWorkflow() != null ? nz(t.getWorkflow().getName()) : "";
        String workflowColor = t.getWorkflow() != null ? nz(t.getWorkflow().getColor()) : "";
        String priority = t.getPriority().getName();    

        return new HomeTaskCard(
                t.getId(),
                nz(t.getTitle()),
                assigneeName,
                assigneeImage,
                startedAt,
                dueDate,
                workflow,
                workflowColor,
                priority
        );
    }
}
