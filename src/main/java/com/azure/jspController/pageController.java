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

