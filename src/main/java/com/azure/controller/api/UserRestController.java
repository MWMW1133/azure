// src/main/java/com/azure/controller/api/UserRestController.java
package com.azure.controller.api;

import com.azure.dto.UserSimpleDTO;
import com.azure.model.user.User;
import com.azure.model.user.User.WorkStatus;
import com.azure.repository.UserRepository;
import com.azure.support.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

  private final UserRepository userRepo;
  private final CurrentUserResolver resolver;

  // 팀원 목록: 같은 조직 구성원 전부(역할/상태 불문) + 본인 제외
@GetMapping
public List<UserSimpleDTO> teammates(Principal principal, HttpSession session) {
    User me = resolver.resolve(principal, session);
    if (me == null || me.getOrganization() == null) {
        return List.of();  // 조직 모르면 빈 배열 (보안)
    }

    final Long orgId = me.getOrganization().getId();
    final Long meId  = me.getId();

    // ✨ 핵심: 조직 기준 '그냥 전부' 불러온 뒤, 본인만 제외. (역할/상태로 걸러서 놓치는 일 방지)
    return userRepo.findByOrganizationIdOrderByNameAsc(orgId).stream()
            .filter(u -> !Objects.equals(u.getId(), meId))  // 본인 제외 (프론트에서도 중복 가드함)
            .map(u -> new UserSimpleDTO(u.getId(), u.getName(), u.getAvatarUrl()))
            .toList();
}

  // 현재 로그인 사용자 id/name
  @GetMapping("/me")
  public Map<String, Object> me(Principal principal, HttpSession session) {
    User me = resolver.resolve(principal, session);
    Long id = (me != null) ? me.getId() : 0L;
    String name = (me != null) ? me.getName() : null;
    return Map.of("id", id, "name", name);
}
}
