package com.azure.jspController;

import com.azure.dto.HomeTaskCard;
import com.azure.dto.TodoItem;
import com.azure.model.calendar.PersonalCalendar;
import com.azure.model.user.User;
import com.azure.service.CalendarService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

        private final CalendarService calendarService;

    @GetMapping("/home")
    public String home(Model model, HttpSession session) {

        User loginUser = (User) session.getAttribute("loginUser");
        Long userId = (loginUser != null) ? loginUser.getId() : null;

        // ===== [A] 오늘 일정 → TO DO 카드 =====
        List<TodoItem> todoList = new ArrayList<>();
        if (userId != null) {
            LocalDate today = LocalDate.now();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.atTime(LocalTime.MAX);

            List<PersonalCalendar> events =
                calendarService.listPersonalEventsBetween(userId, start, end);

            for (PersonalCalendar e : events) {
                String timeText = e.getAllDay() != null && e.getAllDay()
                        ? "종일"
                        : fmtTime(e.getStartAt(), e.getEndAt());

                todoList.add(new TodoItem(
                        e.getTitle(),
                        timeText,
                        Boolean.TRUE.equals(e.getIsDone()) ? "완료" : "미완료",
                        e.getDescription() == null ? "" : e.getDescription()
                ));
            }
        }
        model.addAttribute("todoList", todoList);

        // IN-PROGRESS
        List<HomeTaskCard> inprogressTasks = new ArrayList<>();
        inprogressTasks.add(new HomeTaskCard(
                1L, "디자인 패턴 적용 및 설계", "김담당", "profile1.png",
                "25/09/28", "25/09/28", "in-progress", "normal", 50, true, "25/09/25"
        ));
        inprogressTasks.add(new HomeTaskCard(
                2L, "라이브러리 적용", "이담당", "profile2.png",
                "25/09/28", "25/10/01", "in-progress", "high", 30, true, "25/09/25"
        ));
        model.addAttribute("inprogressTasks", inprogressTasks);

        // DONE
        List<HomeTaskCard> doneTasks = new ArrayList<>();
        doneTasks.add(new HomeTaskCard(
                3L, "UI 디자인", "박담당", "profile3.png",
                "25/09/25", "25/09/28", "completed", "low", 100, true, "25/09/25"
        ));
        doneTasks.add(new HomeTaskCard(
                4L, "UX 디자인 및 리뷰", "최담당", "profile4.png",
                "25/09/25", "25/09/28", "completed", "normal", 100, true, "25/09/25"
        ));
        model.addAttribute("doneTasks", doneTasks);

        model.addAttribute("body", "home.jsp");
        model.addAttribute("activePage", "home");

        return "mainbar";
    }
        private static String fmtTime(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "";
        DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm");
        return start.format(f) + " - " + end.format(f);
    }
}
        
