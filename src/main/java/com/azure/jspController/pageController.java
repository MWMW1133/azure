package com.azure.jspController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class pageController {

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        model.addAttribute("body", "viewProfile.jsp");
        return "mainbar";
    }

    @GetMapping("/noInvite")
    public String noInvite(Model model) {
        model.addAttribute("body", "noInvite.jsp");
        return "mainbar";
    }


    @GetMapping("/meeting")
    public String meeting(Model model) {
        model.addAttribute("body", "meeting.jsp");
        model.addAttribute("activePage", "meeting");
        return "mainbar";
    }

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
    public String projects(Model model) {
        model.addAttribute("projectId", "1234");
        model.addAttribute("projectName", "프로젝트 예시 1234");
        return "project-tab";
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

//        @GetMapping("/event-modal")
//        public String eventModal(Model model) {
//            model.addAttribute("body", "my-calendar-modal.jsp");
//            return "mainbar";
//        }
//
//        @GetMapping("/plan")
//        public String plan(Model model) {
//            model.addAttribute("body", "project-plan.jsp");
//            return "mainbar";
//        }
    }
}


//=========================================
// 나중에 이 버전으로하고 위에거 삭제
// TODO: 나중에 서비스 붙이면 아래 버전으로 교체
//=========================================
//package com.azure.jspController;
//
//import com.azure.dto.UserDTO;
//import com.azure.dto.OrganizationDTO;
//import com.azure.service.UserService;
//import com.azure.service.OrganizationService;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//
//@Controller
//public class PageController {
//
//    private final UserService userService;
//    private final OrganizationService orgService;
//
//    //  생성자 주입
//    public PageController(UserService userService, OrganizationService orgService) {
//        this.userService = userService;
//        this.orgService = orgService;
//    }
//
//    /**
//     * 홈 화면 라우팅
//     * 단순히 home.jsp만 body에 include
//     */
//    @GetMapping("/home")
//    public String home(Model model) {
//        model.addAttribute("body", "home.jsp");
//        return "mainbar"; // /WEB-INF/views/mainbar.jsp
//    }
//
//    /**
//     * 프로필 화면 라우팅
//     * UserService → DB 조회 → JSP에 user/org DTO 전달
//     */
//    @GetMapping("/profile")
//    public String viewProfile(Model model) {
//        // TODO: 로그인 세션에서 userId 가져오기
//        Long userId = userId;
//
//        UserDTO user = userService.findById(userId);
//        OrganizationDTO org = orgService.findById(user.getOrganizationId());
//
//        // JSP에서 접근 가능하도록 모델에 추가
//        model.addAttribute("user", user);
//        model.addAttribute("org", org);
//
//        model.addAttribute("body", "viewProfile.jsp");
//        return "mainbar";
//    }
//}
