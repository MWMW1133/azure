package com.azure.controller.api;

import com.azure.dto.ProjectDTO;
import com.azure.dto.TagDTO;
import com.azure.model.tag.Tag;
import com.azure.service.ProjectService;
import com.azure.service.TagService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.PageRequest;
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
      Long meId = 1L;

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

}