package com.azure.config;

import com.azure.model.OrganizationMember;
import com.azure.model.user.User;
import com.azure.repository.OrganizationMemberRepository;
import com.azure.service.ProjectService;
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
    private final ProjectService projectService;

    @ModelAttribute("user")
    public User addUserToModel(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return null;
        try { return userService.get(loginUser.getId()); }
        catch (Exception e) { return null; }
    }

    @ModelAttribute("org")
    public OrganizationMember addOrganizationToModel(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return null;
        try {
            return organizationMemberRepository.findByUserIdFetchOrganization(loginUser.getId())
                    .stream().findFirst().orElse(null);
        } catch (Exception e) { return null; }
    }

    @ModelAttribute("currentUserId")
    public Long currentUserId(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        return (loginUser != null) ? loginUser.getId() : null;
    }

    // ✅ 사이드바 프로젝트 목록 전역 주입
    @ModelAttribute("projects")
    public java.util.List<com.azure.model.project.Project> sidebarProjects(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return java.util.List.of();
        // 필요에 따라 page 크기/정렬 조정
        var page = projectService.listByUser(loginUser.getId(),
                org.springframework.data.domain.PageRequest.of(0, 200));
        return page.getContent();
    }
}