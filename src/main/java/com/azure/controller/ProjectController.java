package com.azure.controller;

import com.azure.model.project.Project;
import com.azure.model.project.ProjectMember;
import com.azure.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/{id}")
    public Project get(@PathVariable Long id) {
        return projectService.get(id);
    }

    @GetMapping
    public Page<Project> listByUser(@RequestParam Long userId, Pageable pageable) {
        return projectService.listByUser(userId, pageable);
    }

    @PostMapping
    public Project create(@RequestParam Long organizationId,
                          @RequestParam Long ownerId,
                          @RequestParam String name,
                          @RequestParam(required = false) String description) {
        return projectService.create(organizationId, ownerId, name, description);
    }

    @PutMapping("/{id}")
    public Project update(@PathVariable Long id,
                          @RequestParam(required = false) String name,
                          @RequestParam(required = false) String description) {
        return projectService.update(id, name, description);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        projectService.delete(id);
    }

    @PostMapping("/{projectId}/members")
    public ProjectMember addMember(@PathVariable Long projectId,
                                   @RequestParam Long userId,
                                   @RequestParam String role) {
        return projectService.addMember(projectId, userId, role);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public void removeMember(@PathVariable Long projectId,
                             @PathVariable Long userId,
                             @RequestParam Long removedByUserId) {
    projectService.removeMember(projectId, userId, removedByUserId);
    }

}
