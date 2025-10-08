package com.azure.config;

import com.azure.model.OrganizationMember;
import com.azure.model.user.User;
import com.azure.repository.OrganizationMemberRepository;
import com.azure.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

/**
 * 모든 JSP 컨트롤러에서 공통적으로 로그인 사용자 정보를 모델에 추가한다.
 * - 로그인 안 된 경우 user = null
 */
@ControllerAdvice
@RequiredArgsConstructor
public class WebUserAdvice {

    private final UserService userService;
    private final OrganizationMemberRepository organizationMemberRepository;

    @ModelAttribute("user")
    public User addUserToModel(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return null;

        try {
            // 최신 정보 반환 (이름, 프사 변경 반영)
            return userService.get(loginUser.getId());
        } catch (Exception e) {
            return null;
        }
    }

    @ModelAttribute("org")
    public OrganizationMember addOrganizationToModel(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return null;

        try {
            // 유저가 소속된 조직 + 역할 정보 가져오기 (첫 번째 결과 반환)
            return organizationMemberRepository.findByUserIdFetchOrganization(loginUser.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
