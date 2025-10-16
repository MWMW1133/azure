package com.azure.controller.user;

import com.azure.dto.UserDTO;
import com.azure.model.user.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MeApiController {

    /**
     * 현재 로그인된 사용자 정보를 반환하는 API
     * 프론트엔드의 GET /api/me 요청을 이 메서드가 처리합니다.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMe(HttpSession session) {

        // LoginController에서 세션에 저장한 "loginUser" 정보를 가져옵니다.
        User loginUser = (User) session.getAttribute("loginUser");

        // 세션에 사용자 정보가 없으면 로그인되지 않은 상태이므로, 401 에러를 반환합니다.
        if (loginUser == null) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized
        }

        // User 엔티티를 직접 반환하지 않고, 프론트엔드에 필요한 정보만 담은 DTO로 변환합니다.
        // (비밀번호 같은 민감한 정보가 노출되는 것을 방지합니다)
        UserDTO userDto = new UserDTO();
        userDto.setId(loginUser.getId());
        userDto.setName(loginUser.getName());
        userDto.setAvatarUrl(loginUser.getAvatarUrl());
        // 필요하다면 이메일, 조직 ID 등 추가 정보를 담아줍니다.
        if (loginUser.getOrganization() != null) {
            userDto.setOrganizationId(loginUser.getOrganization().getId());
        }

        // 200 OK 상태와 함께 사용자 정보를 반환합니다.
        return ResponseEntity.ok(userDto);
    }
}
