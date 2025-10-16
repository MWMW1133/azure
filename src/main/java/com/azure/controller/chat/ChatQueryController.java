package com.azure.controller.chat;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.azure.dto.ProjectChatDTO;
import com.azure.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatQueryController {

    private final ChatService chatService;

    // 좌측 “그룹 채팅” 목록 — 로그인 사용자(userId)의 프로젝트 채널들
    // GET /api/chat/rooms/projects?userId=1
    @GetMapping("/rooms/projects")
    public List<ProjectChatDTO> listProjectRooms(@RequestParam Long userId) {
        return chatService.listProjectRooms(userId); // ✅ 메서드명 맞춤
    }

    // 특정 프로젝트 클릭 시 채널 보장(없으면 생성) 후 채널ID 반환
    // POST /api/chat/rooms/projects/{projectId}/ensure-channel?me=1&name=옵션
    @PostMapping("/rooms/projects/{projectId}/ensure-channel")
    public Long ensureProjectChannel(@PathVariable Long projectId,
                                     @RequestParam Long me,
                                     @RequestParam(required = false) String name) {
        return chatService.getOrCreateProjectChannel(projectId, name, me);
    }
}
