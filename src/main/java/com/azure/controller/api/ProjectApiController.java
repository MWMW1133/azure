package com.azure.controller.api;

import com.azure.config.WebUserAdvice;
import com.azure.dto.ProjectDTO;
import com.azure.dto.TagDTO;
import com.azure.dto.UserDTO;
import com.azure.model.tag.Tag;
import com.azure.model.user.User;
import com.azure.service.NotificationService;
import com.azure.service.ProjectInvitationService;
import com.azure.service.ProjectService;
import com.azure.service.TagService;
import com.azure.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectApiController {

  private final ProjectService projectService;
  private final TagService tagService;
  private final WebUserAdvice webUserAdvice;
  private final NotificationService notificationService;
  private final UserService userService;
  private final ProjectInvitationService invitationService;

  // ---------- DTO ----------
  @Data
  public static class CreateTagReq { private String name; }

  public static record InviteMembersRequest(List<Long> userIds) {}

  // ---------- 프로젝트 단건 ----------
  @GetMapping("/{projectId}")
  public ProjectDTO get(@PathVariable Long projectId) {
    var p = projectService.get(projectId);
    var dto = new ProjectDTO();
    dto.setId(p.getId());
    dto.setName(p.getName());
    dto.setDescription(p.getDescription());
    dto.setOwnerId(p.getOwner()!=null ? p.getOwner().getId() : null);
    dto.setOrganizationId(p.getOrganization()!=null ? p.getOrganization().getId() : null);
    dto.setStartDate(p.getStartDate());
    dto.setDueDate(p.getDueDate());
    dto.setCreatedAt(p.getCreatedAt());
    return dto;
  }

  // ---------- 내 프로젝트 목록 ----------
  @GetMapping("/list")
  public List<ProjectDTO> myProjects(@ModelAttribute("currentUserId") Long meId) {
    if (meId == null) throw new org.springframework.web.server.ResponseStatusException(
        org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

    var page = projectService.listByUser(
        meId, PageRequest.of(0, 30, Sort.by(Sort.Direction.DESC, "createdAt"))
    );

    return page.getContent().stream().map(p -> {
      var dto = new ProjectDTO();
      dto.setId(p.getId());
      dto.setName(p.getName());
      dto.setDescription(p.getDescription());
      dto.setOwnerId(p.getOwner()!=null ? p.getOwner().getId() : null);
      dto.setOrganizationId(p.getOrganization()!=null ? p.getOrganization().getId() : null);
      dto.setStartDate(p.getStartDate());
      dto.setDueDate(p.getDueDate());
      dto.setCreatedAt(p.getCreatedAt());
      return dto;
    }).toList();
  }

  // ---------- 태그 ----------
  @GetMapping("/{projectId}/tags")
  public List<TagDTO> listTags(@PathVariable Long projectId) {
    return tagService.listByProject(projectId).stream()
        .map(this::toTagDTO)
        .toList();
  }

  @PostMapping("/{projectId}/tags")
  public TagDTO create(@PathVariable Long projectId, @RequestBody CreateTagReq req) {
    Tag t = tagService.create(projectId, req.getName());
    return toTagDTO(t);
  }

  @DeleteMapping("/{projectId}/tags/{tagId}")
  public void delete(@PathVariable Long projectId, @PathVariable Long tagId) {
    tagService.delete(projectId, tagId);
  }

  private TagDTO toTagDTO(Tag tag) {
    if (tag == null) return null;
    TagDTO dto = new TagDTO();
    dto.setId(tag.getId());
    dto.setName(tag.getName());
    dto.setColor(tag.getColor());
    dto.setCreatedAt(tag.getCreatedAt());
    if (tag.getProject() != null) dto.setProjectId(tag.getProject().getId());
    return dto;
  }

  // ---------- 멤버 ----------
  @GetMapping("/{projectId}/members")
  public List<UserDTO> projectMembers(@PathVariable Long projectId,
                                      @RequestParam(required = false) String query) {
    var page = projectService.listMembers(projectId, PageRequest.of(0, 200, Sort.unsorted()));

    Map<Long, User> users = new LinkedHashMap<>();
    page.getContent().forEach(pm -> {
      if (pm.getUser() != null) users.putIfAbsent(pm.getUser().getId(), pm.getUser());
    });

    return users.values().stream()
        .filter(u -> {
          if (query == null || query.isBlank()) return true;
          String q = query.toLowerCase();
          return (u.getName() != null && u.getName().toLowerCase().contains(q));
        })
        .map(u -> {
          var dto = new UserDTO();
          dto.setId(u.getId());
          dto.setName(u.getName());
          dto.setAvatarUrl(u.getAvatarUrl());
          return dto;
        })
        .toList();
  }

  @GetMapping("/{projectId}/invitations")
  public List<Long> listProjectInvitations(@PathVariable Long projectId) {
    return invitationService.getPendingInvitedUserIds(projectId);
  }

  @PostMapping("/{projectId}/invitations")
  public ResponseEntity<Void> inviteAsMembers(@PathVariable Long projectId,
                                              @RequestBody InviteMembersRequest req,
                                              HttpSession session) {
    Long userId = webUserAdvice.currentUserId(session);  // 권한 체크는 이후 보강
    User me = userService.get(userId);

    if (req.userIds() == null || req.userIds().isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    for (Long uid : req.userIds()) {

      if (projectService.existsMember(projectId, uid)) continue; // 이미 멤버면 스킵
      projectService.addMember(projectId, uid, me.getName());

    }
    return ResponseEntity.noContent().build();
  }
}
