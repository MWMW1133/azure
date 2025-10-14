package com.azure.support;

import com.azure.model.user.User;
import com.azure.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class CurrentUserResolver {
  private final UserRepository userRepo;

  public CurrentUserResolver(UserRepository userRepo) {
    this.userRepo = userRepo;
  }

  /** Principal → SecurityContext → HttpSession 순서로 현재 로그인 사용자 찾기 */
  public User resolve(Principal principal, HttpSession session) {
    // 1) Principal (표준 세션 로그인)
    if (principal != null) {
      var u = userRepo.findByLoginId(principal.getName()).orElse(null);
      if (u != null) return u;
    }

    // 2) SecurityContext (CustomUserDetails 등)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated()) {
      Object p = auth.getPrincipal();
      if (p instanceof User u) return u;
      // if (p instanceof CustomUserDetails cud) {
      //   return userRepo.findById(cud.getId()).orElse(null);
      // }
    }

    // 3) HttpSession 다양한 키 탐색 (프로젝트별로 다를 수 있음)
    if (session != null) {
      // 3-1) User 객체로 저장된 경우
      Object sesUser = session.getAttribute("user");        // 기존
      if (sesUser instanceof User u) return u;

      Object loginUser = session.getAttribute("loginUser"); // 다른 팀서 자주 쓰는 키
      if (loginUser instanceof User u) return u;

      // 3-2) 숫자 ID로 저장된 경우
      Long id = tryGetLong(session.getAttribute("userId"));
      if (id == null) id = tryGetLong(session.getAttribute("loginUserId"));
      if (id != null && id > 0) {
        var u = userRepo.findById(id).orElse(null);
        if (u != null) return u;
      }

      // 3-3) loginId(이메일/아이디) 문자열로 저장된 경우
      String loginId = tryGetString(session.getAttribute("loginId"));
      if (loginId == null) loginId = tryGetString(session.getAttribute("login_id"));
      if (loginId != null && !loginId.isBlank()) {
        var u = userRepo.findByLoginId(loginId).orElse(null);
        if (u != null) return u;
      }
    }

    return null;
  }

  private Long tryGetLong(Object o) {
    if (o instanceof Number n) return n.longValue();
    if (o instanceof String s) {
      try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
    }
    return null;
  }

  private String tryGetString(Object o) {
    return (o instanceof String s) ? s : null;
  }
}
