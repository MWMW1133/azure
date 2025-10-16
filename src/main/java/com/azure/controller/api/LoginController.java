package com.azure.controller.api;

import com.azure.model.user.User;
import com.azure.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /** 로그인 폼 */
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("error", null);
        return "login"; // /WEB-INF/views/login.jsp
    }

    /** 로그인 처리 */
    @PostMapping("/login")
    public String login(@RequestParam String userId,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        return userService.findByLoginId(userId)
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .map(user -> {

                    if (user.getOrganization() == null) {
                        // 조직이 없는 일반 유저는 초대 전 전용 페이지로
                        session.setAttribute("loginUser", user);
                        return "redirect:/noInvitePage";
                    } else {
                        // 조직 소속 유저는 홈으로
                        session.setAttribute("loginUser", user);
                        return "redirect:/home";
                    }
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
                    return "login";
                });
    }

    /** 회원가입 폼 */
    @GetMapping("/signup")
    public String signupForm() {
        return "signup"; // /WEB-INF/views/signup.jsp
    }


    /** 회원가입 처리 */
    @PostMapping("/signup")
    public String signup(@RequestParam String userId,
                         @RequestParam String userPwd,
                         @RequestParam String userName,
                         @RequestParam(required = false) String companyName,
                         // @RequestParam(required = false) String Long orgId,
                         Model model) {
        try {

            System.out.println("[DEBUG] 회원가입 요청 들어옴: " + userId + "/" + userName);

            // 비밀번호 해시
            String passwordHash = passwordEncoder.encode(userPwd);

            // 관리자 가입 / 구성원 가입 판단
            boolean isAdminSignup = (companyName != null && !companyName.isBlank());

            // 기본 프사
            String defaultAvatarUrl = "/images/profile1.png";

            // 사용자 생성
            userService.create(
                    null,                // organizationId (구성원 가입 시 확장 가능)
                    // orgId,
                    userId,
                    passwordHash,
                    userName,
                    defaultAvatarUrl,
                    User.WorkStatus.WORKING,
                    isAdminSignup,
                    companyName
            );

            return "redirect:/login"; // 가입 성공 후 로그인 페이지
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "signup"; // 가입 실패 시 다시 회원가입 폼
        }
    }

    /** 로그아웃 */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
