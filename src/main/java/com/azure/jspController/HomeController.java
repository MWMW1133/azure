package com.azure.jspController;

import java.util.ArrayList;
import java.util.List;

import com.azure.model.user.User;
import com.azure.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.azure.dto.TodoItem;
import com.azure.dto.HomeTaskCard; // ★ 추가

@Controller
public class HomeController {


    @GetMapping("/home")
    public String home(Model model, HttpSession session) {

        // 기존 더미 데이터=====================
        List<TodoItem> todoList = new ArrayList<>();
        todoList.add(new TodoItem("레퍼런스 찾기", "15:00 - 16:00", "미완료", "시각 자료 위주"));
        todoList.add(new TodoItem("클라이언트 원격", "16:30 - 17:30", "미완료", "내용 정리하기"));
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
}