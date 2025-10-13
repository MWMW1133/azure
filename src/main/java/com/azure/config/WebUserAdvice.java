package com.azure.config;

import com.azure.model.OrganizationMember;
import com.azure.model.user.User;
import com.azure.repository.OrganizationMemberRepository;
import com.azure.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import static com.azure.security.SecurityUtil.getCurrentUserId;


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
    public User addUserToModel() {
        Long uid = getCurrentUserId();
        if (uid == null) return null;
        try { return userService.get(uid); } catch (Exception e) { return null; }
    }

    @ModelAttribute("org")
    public OrganizationMember addOrganizationToModel() {
        Long uid = getCurrentUserId();
        if (uid == null) return null;
        try {
            return organizationMemberRepository.findByUserIdFetchOrganization(uid)
                    .stream().findFirst().orElse(null);
        } catch (Exception e) { return null; }
    }

    @ModelAttribute("currentUserId")
    public Long exposeCurrentUserId() {
        return getCurrentUserId(); // 로그인 안 됐으면 null
    }

}
