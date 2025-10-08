package com.azure.jspController;


import com.azure.model.user.User;
import com.azure.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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

    /**
     * [페이지 진입]
     * - 현재 로그인한 사용자의 조직 멤버 목록
     * - 아직 조직에 속하지 않은 사용자 목록
     */
    /** 모달용 fragment */
    @GetMapping("/modal")
    public String inviteModal(@org.springframework.web.bind.annotation.ModelAttribute("user") User user,
                              Model model) {
        if (user == null) return "redirect:/login";

        Long orgId = (user.getOrganization() != null) ? user.getOrganization().getId() : null;
        List<User> orgMembers = (orgId != null)
                ? userService.listByOrganization(orgId)
                : List.of();

        List<User> invitableUsers = userService.searchInvitableUsers("");

        model.addAttribute("orgMembers", orgMembers);
        model.addAttribute("invitableUsers", invitableUsers);

        // JSP fragment 경로 (WEB-INF/views/invite-colleagues-modal.jsp)
        return "inviteColleagues";
    }
//    @GetMapping("/invite")
//    public String invitePage(@org.springframework.web.bind.annotation.ModelAttribute("user") User user,
//                             Model model) {
//        if (user == null) {
//            return "redirect:/login";
//        }
//
//        // [1] 현재 로그인 사용자의 조직 ID
//        Long orgId = (user.getOrganization() != null) ? user.getOrganization().getId() : null;
//
//        // [2] 현재 조직의 멤버 (관리자/구성원 모두)
//        List<User> orgMembers = (orgId != null)
//                ? userService.listByOrganization(orgId)
//                : List.of();
//
//        // [3] 조직에 속하지 않은 사용자 (초대 가능 대상)
//        List<User> invitableUsers = userService.searchInvitableUsers("");
//
//        // JSP 전달
//        model.addAttribute("orgMembers", orgMembers);
//        model.addAttribute("invitableUsers", invitableUsers);
//
//        // mainbar 레이아웃
//        model.addAttribute("body", "invite-colleagues.jsp");
//        model.addAttribute("activePage", "settings");
//        return "inviteColleagues.jsp";
//    }



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