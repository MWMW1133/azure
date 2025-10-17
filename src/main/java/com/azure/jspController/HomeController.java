// src/main/java/com/azure/jspController/HomeController.java
package com.azure.jspController;

import com.azure.config.WebUserAdvice;
import com.azure.dto.HomeTaskCard;
import com.azure.dto.TodoItem;
import com.azure.model.calendar.PersonalCalendar;
import com.azure.model.task.Task;
import com.azure.service.CalendarService;
import com.azure.service.TaskService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final WebUserAdvice webUserAdvice;
    private final TaskService taskService;
    private final CalendarService calendarService;

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yy-MM-dd");
    private static final DateTimeFormatter HM  = DateTimeFormatter.ofPattern("HH:mm");
    private static final int DUE_SOON_DAYS = 3;

    @GetMapping("/home")
    public String home(Model model, HttpSession session) {
        Long userId = webUserAdvice.currentUserId(session);
        if (userId == null) return "redirect:/login";

        int pageSize = 50;
        List<Task> myTasks = taskService
                .listByAssignee(userId, PageRequest.of(0, pageSize))
                .getContent();

        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(DUE_SOON_DAYS);

        // 마감 임박 (진행중)
        List<HomeTaskCard> inprogressTasks = myTasks.stream()
                .filter(t -> t.getWorkflow() == null || !Boolean.TRUE.equals(t.getWorkflow().getIsTerminal()))
                .filter(t -> t.getDueDate() != null
                        && !t.getDueDate().isBefore(today)
                        && !t.getDueDate().isAfter(until))
                .sorted(Comparator
                        .comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Task::getId))
                .map(this::toCard)
                .collect(Collectors.toList());

        // 긴급(우선순위 5)
        List<HomeTaskCard> doneTasks = myTasks.stream()
                .filter(t -> t.getPriority() != null && t.getPriority().getId() == 5)
                .sorted(Comparator
                        .comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Task::getId))
                .map(this::toCard)
                .collect(Collectors.toList());

        // ===== 오늘 일정 → 홈의 To-do 카드용 =====
        List<TodoItem> todoList = new ArrayList<>();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end   = today.atTime(LocalTime.MAX);

        List<PersonalCalendar> events =
                calendarService.listPersonalEventsBetween(userId, start, end);

        for (PersonalCalendar e : events) {
            String timeText = Boolean.TRUE.equals(e.getAllDay())
                    ? "종일"
                    : fmtTime(e.getStartAt(), e.getEndAt());
            todoList.add(new TodoItem(
                    e.getTitle(),
                    timeText,
                    Boolean.TRUE.equals(e.getIsDone()) ? "완료" : "미완료",
                    e.getDescription() == null ? "" : e.getDescription()
            ));
        }

        model.addAttribute("todoList", todoList);
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
        String startedAt     = t.getStartDate() != null ? t.getStartDate().format(YMD) : "";
        String dueDate       = t.getDueDate()   != null ? t.getDueDate().format(YMD)   : "";
        String workflow      = t.getWorkflow() != null ? nz(t.getWorkflow().getName()) : "";
        String workflowColor = t.getWorkflow() != null ? nz(t.getWorkflow().getColor()) : "";
        String priority      = t.getPriority().getName();

        return new HomeTaskCard(
                t.getId(), nz(t.getTitle()), assigneeName, assigneeImage,
                startedAt, dueDate, workflow, workflowColor, priority
        );
    }

    private static String fmtTime(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "";
        return start.format(HM) + " - " + end.format(HM);
    }
}