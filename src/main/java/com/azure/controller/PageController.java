package com.azure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    
    // 로그인 페이지
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // 회원가입 페이지
    @GetMapping("/signup")
    public String signup() {
        return "signup"; 
    }

    @GetMapping("/")
    public String mainbar() {
        return "mainbar"; // /WEB-INF/views/mainbar.jsp 로 forward
    }

    @GetMapping("/calendar")
    public String calendar() {
        return "my-calendar"; // /WEB-INF/views/my-calendar.jsp
    }

    @GetMapping("/tasks")
    public String tasks() {
        return "my-tasks"; // /WEB-INF/views/my-tasks.jsp
    }

    @Controller
    public class ModalController {
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
