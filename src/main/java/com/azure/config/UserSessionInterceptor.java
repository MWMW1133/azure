package com.azure.config;

import com.azure.model.user.User;
import com.azure.security.CustomUserDetails;
import com.azure.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class UserSessionInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();

        // 세션에 이미 loginUser가 있으면 아무것도 안 함
        if (session.getAttribute("loginUser") != null) {
            return true;
        }

        // Spring Security 컨텍스트에서 인증 정보 가져오기
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 있고, principal이 CustomUserDetails 타입일 때만 실행
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {

            // DB에서 최신 User 정보를 가져와 세션에 저장 (WebUserAdvice가 사용할 수 있도록)
            User loginUser = userService.get(userDetails.getId());
            session.setAttribute("loginUser", loginUser);
        }

        return true;
    }
}

