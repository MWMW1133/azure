package com.azure.controller.project;

import com.azure.model.project.Project;
import com.azure.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import static com.azure.security.SecurityUtil.getCurrentUserId;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final ProjectService projectService;

    /**
     * 모든 뷰에서 ${projects} 사용 가능
     */

    @ModelAttribute("projects")
    public List<Project> populateProjects() {
        // TODO: 로그인 붙으면 세션에서 userId 꺼내오기
        Long userId = getCurrentUserId(); // 임시 하드코딩
        return projectService.listByUser(userId, Pageable.unpaged()).getContent();
    }

}
