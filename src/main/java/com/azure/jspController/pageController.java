package com.azure.jspController;

import com.azure.model.user.User;
import com.azure.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/profile")
    public String viewProfile(@ModelAttribute("user") User user, Model model) {
        if (user == null) return "redirect:/login";

        // model.addAttribute("activePage", "profile");
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

//    @GetMapping("/tasks")
//    public String tasks(Model model) {
//        model.addAttribute("body", "my-tasks.jsp");
//        model.addAttribute("activePage", "tasks");
//        return "mainbar";
//    }



 @GetMapping("/projects")
    public String projects() {
        return "project-tab"; // /WEB-INF/views/my-tasks.jsp
    }

    // 메인 테이블 탭
    @GetMapping("/projects/{projectId}/table")
    public String projectTable(@PathVariable String projectId, Model model) {
        // --- 데모 태스크 1 (하위 태스크 있음) ---
        Map<String, Object> sub1 = new LinkedHashMap<>();
        sub1.put("id", "t-1-1");
        sub1.put("title", "하위 태스크 1");
        sub1.put("assigneeImage", "avatar1.png"); // /images/avatar1.png 가정
        sub1.put("assigneeName", "김민준");
        sub1.put("startedAt", "25/09/24");
        sub1.put("dueDate", "25/09/26");
        sub1.put("status", "Reviewing"); // CSS 클래스용
        sub1.put("priority", "high");    // highest/high/normal/low/lowest
        sub1.put("processPct", 30);
        sub1.put("hasFile", Boolean.TRUE);
        sub1.put("updatedAt", "25/09/25");
        sub1.put("subTasks", null);      // 더 깊이 없음

        Map<String, Object> t1 = new LinkedHashMap<>();
        t1.put("id", "t-1");
        t1.put("title", "태스크 이름 1");
        t1.put("assigneeImage", "avatar2.png");
        t1.put("assigneeName", "이서연");
        t1.put("startedAt", "25/09/25");
        t1.put("dueDate", "25/09/29");
        t1.put("status", "In-Progress");  // 자유롭게: InProgress/Reviewing/Completed 등
        t1.put("priority", "normal");
        t1.put("processPct", 50);
        t1.put("hasFile", Boolean.TRUE);
        t1.put("updatedAt", "25/09/25");
        t1.put("subTasks", List.of(sub1)); // 하위 태스크 1개

        // --- 데모 태스크 2 (하위 태스크 없음) ---
        Map<String, Object> t2 = new LinkedHashMap<>();
        t2.put("id", "t-2");
        t2.put("title", "태스크 이름 2");
        t2.put("assigneeImage", "avatar3.png");
        t2.put("assigneeName", "박도윤");
        t2.put("startedAt", "25/09/20");
        t2.put("dueDate", "25/09/30");
        t2.put("status", "Reviewing");
        t2.put("priority", "high");
        t2.put("processPct", 30);
        t2.put("hasFile", Boolean.FALSE);
        t2.put("updatedAt", "25/09/25");
        t2.put("subTasks", null);

        // --- 완료 태스크 1 ---
        Map<String, Object> t3 = new LinkedHashMap<>();
        t3.put("id", "t-3");
        t3.put("title", "완료 태스크 1");
        t3.put("assigneeImage", "avatar4.png");
        t3.put("assigneeName", "최아린");
        t3.put("startedAt", "25/09/10");
        t3.put("dueDate", "25/09/15");
        t3.put("status", "Completed");
        t3.put("priority", "low");
        t3.put("processPct", 100);
        t3.put("hasFile", Boolean.TRUE);
        t3.put("updatedAt", "25/09/15");
        t3.put("subTasks", null);

        model.addAttribute("projectId", projectId);
        model.addAttribute("activeTasks", List.of(t1, t2));
        model.addAttribute("archivedTasks", List.of(t3));
        return "projects/mainTable";
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

