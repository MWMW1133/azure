package com.azure.controller.api;

import com.azure.dto.UserSimpleDTO;
import com.azure.model.user.User;
import com.azure.repository.UserRepository;
import com.azure.service.UserService;
import com.azure.support.CurrentUserResolver;
import com.azure.config.WebUserAdvice;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;                    // java.nio.file.Path !
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

  private final UserRepository userRepo;
  private final CurrentUserResolver resolver;
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final WebUserAdvice webUserAdvice;

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

  // 비밀번호 변경
  @PatchMapping(value = "/password", consumes = "application/json")
    public ResponseEntity<Void> changePassword(@RequestBody @Validated ChangePasswordReq req, HttpSession session) {
        // 현재 로그인 사용자 ID
        Long currentUserId = webUserAdvice.currentUserId(session);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }

        // 사용자 조회
        User u = userService.get(currentUserId); // NotFoundException 발생 시 404 핸들러로

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(req.getCurrentPassword(), u.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        if (passwordEncoder.matches(req.getNewPassword(), u.getPasswordHash())) {
            throw new IllegalArgumentException("이전과 동일한 비밀번호는 사용할 수 없습니다.");
        }

        // 새 비밀번호 해시 생성
        String newHash = passwordEncoder.encode(req.getNewPassword());

        // update
        userService.update(u.getId(), newHash, null, null, null);

        return ResponseEntity.noContent().build(); 
    }

    @Data
    public static class ChangePasswordReq {
        private String currentPassword;
        private String newPassword;
    }

    @Value("${app.upload.dir}")
    private String uploadDir;

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadAvatar(@RequestPart("file") MultipartFile file,
                                            Principal principal, HttpSession session) throws IOException {
        User me = resolver.resolve(principal, session);
        if (me == null) throw new IllegalStateException("로그인된 사용자가 없습니다.");
        if (file.isEmpty()) throw new IllegalArgumentException("파일이 비었습니다.");

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String safeExt = (ext != null ? ext.toLowerCase() : "png");
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = "avatar_" + me.getId() + "_" + ts + "." + safeExt;

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

                                              
        return Map.of("url", "/images/" + filename);
    }

    @PatchMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileReq req,
                                              Principal principal, HttpSession session) {
        User me = resolver.resolve(principal, session);
        if (me == null) return ResponseEntity.status(401).build();

        User.WorkStatus ws = null;
        if (req.getWorkStatus() != null) {
            for (User.WorkStatus v : User.WorkStatus.values()) {
                if (v.name().equalsIgnoreCase(req.getWorkStatus())) { ws = v; break; }
            }
        }

        if (req.getName() != null && !req.getName().isBlank()) me.setName(req.getName().trim());
        if (req.getAvatarUrl() != null && !req.getAvatarUrl().isBlank()) me.setAvatarUrl(req.getAvatarUrl());
        if (ws != null) me.setWorkStatus(ws);

        userRepo.save(me);
        return ResponseEntity.noContent().build(); 
    }

    @Data
    public static class ProfileReq {
        private String name;
        private String avatarUrl;   
        private String workStatus;  
    }
}
