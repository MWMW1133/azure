package com.azure.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;

public class SecurityUtil {

    private static CustomUserDetails currentPrincipalOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;
        if (authentication instanceof AnonymousAuthenticationToken) return null;

        Object principal = authentication.getPrincipal();
        return (principal instanceof CustomUserDetails) ? (CustomUserDetails) principal : null;
    }

    /** 현재 로그인한 사용자 ID (없으면 null) */
    public static Long getCurrentUserId() {
        CustomUserDetails cud = currentPrincipalOrNull();
        return (cud != null) ? cud.getId() : null;
    }

    /** 현재 로그인한 사용자의 조직 ID (없으면 0L 혹은 null로 정책 맞추기) */
    public static Long getOrganizationId() {
        CustomUserDetails cud = currentPrincipalOrNull();
        return (cud != null) ? cud.getOrganizationId() : 0L; // 필요하면 null 반환으로 바꿔도 됨
    }

    /** 현재 로그인한 사용자의 loginId (없으면 null) */
    public static String getCurrentLoginId() {
        CustomUserDetails cud = currentPrincipalOrNull();
        return (cud != null) ? cud.getUsername() : null;
    }

        /** 없으면 401 던지는 guard */
    public static Long requireUserId() {
        Long id = getCurrentUserId();
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return id;
    }

    /** (개발용) 시큐리티→세션 순으로 조회, 없으면 null */
    public static Long resolveUserId(HttpSession session) {
        Long id = getCurrentUserId();
        if (id != null) return id;
        Object v = (session != null)
                ? (session.getAttribute("id") != null ? session.getAttribute("id") : session.getAttribute("idKey"))
                : null;
        return (v != null) ? Long.valueOf(v.toString()) : null;
    }

    /** (개발용) 시큐리티→세션 순으로 조회, 없으면 401 */
    public static Long requireUserId(HttpSession session) {
        Long id = resolveUserId(session);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return id;
    }
}
