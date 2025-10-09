package com.azure.controller.api;

import com.azure.model.OrganizationMember;
import com.azure.model.enums.OrganizationRole;
import com.azure.model.user.User;
import com.azure.repository.OrganizationMemberRepository;
import com.azure.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/invite")
@RequiredArgsConstructor
public class InviteApiController {

    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationRepository organizationRepository;

    /** 초대 수락 */
    @PostMapping("/accept")
    public Map<String, Object> acceptInvite(@ModelAttribute("user") User user,
                                            @RequestBody Map<String, Object> body) {
        Long orgId = Long.valueOf(body.get("organizationId").toString());

        // 이미 구성원인지 확인
        boolean exists = organizationMemberRepository.existsByOrganizationIdAndUserId(orgId, user.getId());
        if (exists) {
            return Map.of("status", "error", "message", "이미 해당 조직의 구성원입니다.");
        }

        // 조직 멤버로 추가
        var org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("조직을 찾을 수 없습니다."));
        organizationMemberRepository.save(new OrganizationMember(org, user, OrganizationRole.MEMBER));

        return Map.of("status", "success", "message", org.getName() + " 조직 초대를 수락했습니다.");
    }

    /** 초대 거절 */
    @PostMapping("/reject")
    public Map<String, Object> rejectInvite(@ModelAttribute("user") User user,
                                            @RequestBody Map<String, Object> body) {
        // 단순히 성공 메시지만 반환 (상태 저장 안 함)
        return Map.of("status", "success", "message", "조직 초대를 거절했습니다.");
    }
}
