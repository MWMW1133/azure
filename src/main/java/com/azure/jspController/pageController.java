package com.azure.jspController;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class pageController {

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("body", "home.jsp");
        return "mainbar"; // /WEB-INF/views/.jsp
    }

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        model.addAttribute("body", "viewProfile.jsp");
        return "mainbar";
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
//    // 💡 생성자 주입
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
//        // TODO: 로그인 세션에서 userId 가져오기 (임시로 1번 사용자)
//        Long userId = 1L;
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
