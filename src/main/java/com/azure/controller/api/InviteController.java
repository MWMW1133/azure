package com.azure.controller.api;


import com.azure.model.OrganizationMember;
import com.azure.model.enums.OrganizationRole;
import com.azure.model.notify.NotificationType;
import com.azure.model.user.User;
import com.azure.repository.OrganizationMemberRepository;
import com.azure.service.NotificationService;
import com.azure.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 회사 신입 초대 (관리자 전용)
 * - 관리자만 접근 가능 (접근 제어는 WebSecurityConfig 또는 JSP에서 처리 가능)
 * - /invite 페이지에서 현재 조직 멤버와 초대 가능 사용자 조회
 */
@Controller
@RequestMapping("/invite")
@RequiredArgsConstructor
public class InviteController {

    private final UserService userService;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final NotificationService notificationService;

    /**
     * [페이지 진입]
     * - 현재 로그인한 사용자의 조직 멤버 목록
     * - 아직 조직에 속하지 않은 사용자 목록
     */
    /** 모달용 fragment */
    @GetMapping("/modal")
    public String inviteModal(@ModelAttribute("user") User user,
                              Model model) throws AccessDeniedException {
        if (user == null) return "redirect:/login";

        // 관리자만
        Optional<OrganizationMember> memberOpt =
                organizationMemberRepository.
                        findByUserIdFetchOrganization(user.getId())
                        .stream().
                        findFirst();

        if (memberOpt.isEmpty() || memberOpt.get().getRole() != OrganizationRole.MANAGER) {
            throw new AccessDeniedException("관리자만 접근할 수 있습니다.");
        }


//        List<OrganizationMember> test = organizationMemberRepository.findByUserIdFetchOrganization(user.getId());
//        System.out.println("[DEBUG] findByUserIdFetchOrganization size = " + test.size());


        Long orgId = (user.getOrganization() != null)
                ? user.getOrganization().getId()
                : null;

        List<OrganizationMember> orgMembers = (orgId != null)
                ? organizationMemberRepository.findByOrganizationIdFetchUser(orgId)
                : List.of();

        model.addAttribute("orgMembers", orgMembers);
        model.addAttribute("invitableUsers", List.of());


        return "inviteColleagues";
    }


    /**
     *  초대보내깅
     */
    @PostMapping(value = "/send", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Map<String, Object> sendInvitations(
            @ModelAttribute("user") User sender,
            @RequestBody(required = false) Map<String, Object> body
    ) {


        if (sender == null) {
            return Map.of("status", "error", "message", "로그인이 필요합니다.");
        }

        if (body == null) {
            return Map.of("status", "error", "message", "요청 바디가 비었습니다.");
        }

        // 디버깅좀
        System.out.println("[DEBUG] /invite/send body keys = " + body.keySet());

        // userIds, userId 둘 다 가능
        Object raw = body.get("userIds");
        if (raw == null) raw = body.get("userId");

        List<Long> userIds = toLongList(raw);
        if (userIds.isEmpty()) {
            return Map.of("status", "error", "message", "초대 대상이 없습니다.");
        }

        // 관리자만
        var memberOpt = organizationMemberRepository.findByUserIdFetchOrganization(sender.getId())
                .stream().findFirst();

        if (memberOpt.isEmpty() || memberOpt.get().getRole() != OrganizationRole.MANAGER) {
            return Map.of("status", "error", "message", "관리자만 초대할 수 있습니다.");
        }

        // 초대 처리 및 알림 생성
        int successCount = 0;
        for (Long targetId : userIds) {
            // 알림 생성
            String payload = String.format(
                    "{\"sender\":\"%s\",\"organization\":\"%s\",\"link\":\"/organization/invitations\"}",
                    sender.getName(),
                    memberOpt.get().getOrganization().getName()
            );

            notificationService.notifyUser(
                    targetId,
                    NotificationType.INVITE_ORGANIZATION.name(),
                    payload
            );

            successCount++;
            System.out.println("[INVITE] from=" + sender.getName() + " → to userId=" + targetId);
        }

        return Map.of("status", "success", "count", successCount);
    }


    // --------- 유틸: 무엇이 와도 Long 리스트로 바꿔줌 ----------
    @SuppressWarnings("unchecked")
    private List<Long> toLongList(Object raw) {
        if (raw == null) return List.of();

        // 배열/리스트인 경우
        if (raw instanceof List<?> list) {
            List<Long> out = new java.util.ArrayList<>(list.size());
            for (Object v : list) {
                Long lv = toLong(v);
                if (lv != null) out.add(lv);
            }
            return out;
        }

        // 단일 값인 경우
        Long single = toLong(raw);
        return (single == null) ? List.of() : List.of(single);
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s && !s.isBlank()) {
            try { return Long.valueOf(s.trim()); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    /**
     * [검색 AJAX]
     * - 사용자 이름, ID, 팀명 등으로 검색
     * - 조직 미가입자만 조회
     */
    @GetMapping("/search")
    @ResponseBody
    public List<User> searchInvitableUsers(@RequestParam("keyword") String keyword) {
        return userService.searchInvitableUsers(keyword);
    }
}