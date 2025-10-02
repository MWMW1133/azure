package com.azure.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.azure.dto.Task; 
import com.azure.dto.TodoItem;

@Controller
public class HomeController {

    // home
    @GetMapping("/home")
    public String home(Model model) {
        
        // --- 더미 데이터 생성 ---
        
        model.addAttribute("userName", "홍길동"); // JSP와 일관성을 위해 이름 변경

        // TO-DO 리스트 데이터 (이 부분은 기존 TodoItem DTO를 사용하므로 그대로 둡니다)
        List<TodoItem> todoList = new ArrayList<>();
        todoList.add(new TodoItem("레퍼런스 찾기", "15:00 - 16:00", "미완료", "시각 자료 위주"));
        todoList.add(new TodoItem("클라이언트 원격", "16:30 - 17:30", "미완료", "내용 정리하기"));
        model.addAttribute("todoList", todoList);
        
        // --- Task DTO 구조에 맞게 데이터 생성 수정 ---

        // IN-PROGRESS 리스트 데이터
        List<Task> inprogressTasks = new ArrayList<>();
        // Task(Long id, String title, String description, String assigneeName, String assigneeImage, String dueDate, String status, String priority, int processPct, String updatedAt)
        inprogressTasks.add(new Task(1L, "디자인 패턴 적용 및 설계", "김담당", "profile1.png", "25/09/28","25/09/28", "in-progress", "normal", 50, true, "25/09/25"));
        inprogressTasks.add(new Task(2L, "라이브러리 적용", "이담당", "profile2.png", "25/09/28","25/10/01", "in-progress", "high", 30, true,"25/09/25"));
        model.addAttribute("inprogressTasks", inprogressTasks);

        // DONE 리스트 데이터
        List<Task> doneTasks = new ArrayList<>();
        doneTasks.add(new Task(3L, "UI 디자인", "박담당", "profile3.png", "25/09/25","25/09/28", "completed", "low", 100, true,"25/09/25"));
        doneTasks.add(new Task(4L, "UX 디자인 및 리뷰", "최담당", "profile4.png", "25/09/25","25/09/28", "completed", "normal", 100, true,"25/09/25"));
        model.addAttribute("doneTasks", doneTasks);

        // ==== 수정 return "home";
        // 현재 페이지 식별용
        model.addAttribute("body", "home.jsp");
        model.addAttribute("activePage", "home");

        return "mainbar"; // ★ 포인트
    }
}