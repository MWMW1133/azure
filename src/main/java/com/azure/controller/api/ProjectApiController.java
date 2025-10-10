package com.azure.controller.api;

import com.azure.dto.ProjectDTO;
import com.azure.dto.TagDTO;
import com.azure.dto.UserDTO;
import com.azure.model.tag.Tag;
import com.azure.model.user.User;
import com.azure.service.ProjectService;
import com.azure.service.TagService;
import static com.azure.security.SecurityUtil.getCurrentUserId;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectApiController {
    private final ProjectService projectService;
    private final TagService tagService;
    
    @Data
    public static class CreateTagReq {
        private String name;
    }

    // 프로젝트 리스트
    @GetMapping("/list")
    public List<ProjectDTO> myProjects() {
      Long meId = getCurrentUserId();

      var page = projectService.listByUser(
          meId,
          PageRequest.of(0, 30, Sort.by(Sort.Direction.DESC, "createdAt"))
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

    // 태그 리스트
    @GetMapping("/{projectId}/tags")
    public List<TagDTO> listTags(@PathVariable Long projectId) {
        return tagService.listByProject(projectId)
                .stream()
                .map(this::toTagDTO)
                .toList(); 
    }
    
    // 태그 저장
    @PostMapping("/{projectId}/tags")
    public TagDTO create(@PathVariable Long projectId, @RequestBody CreateTagReq req) {
        Tag t = tagService.create(projectId, req.getName());
        return toTagDTO(t);
    }

    // 태그 삭제
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
        if (tag.getProject() != null) {
            dto.setProjectId(tag.getProject().getId());
        }
        return dto;
    }

    @GetMapping("/{projectId}/members") 
    public List<UserDTO> projectMembers(@PathVariable Long projectId,
                                        @RequestParam(required = false) String query) {
        // 정렬이 불가하면 Sort.unsorted() 해도 됨
        var page = projectService.listMembers(projectId, PageRequest.of(0, 200, Sort.unsorted()));

        // 중복 제거(같은 유저가 중복 안 오더라도 안전하게)
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
}