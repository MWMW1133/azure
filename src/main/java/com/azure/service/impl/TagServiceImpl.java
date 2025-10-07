package com.azure.service.impl;

import com.azure.model.project.Project;
import com.azure.model.tag.Tag;
import com.azure.repository.ProjectRepository;
import com.azure.repository.TagRepository;
import com.azure.service.TagService;
import com.azure.service.exception.NotFoundException; 
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Tag> listByProject(Long projectId) {
        return tagRepository.findByProjectId(projectId);
    }

    @Override
    public Tag create(Long projectId, String name) {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) throw new IllegalArgumentException("태그명은 비어 있을 수 없습니다.");
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        // 중복 체크
        if (tagRepository.existsByProjectIdAndName(projectId, n)) {
            throw new IllegalArgumentException("이미 존재하는 태그입니다: " + n);
        }

        Tag t = new Tag();
        t.setProject(project);
        t.setName(n);
        return tagRepository.save(t);
    }

    @Override
    public void delete(Long projectId, Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Tag not found: " + tagId));

        if (tag.getProject() == null || !projectId.equals(tag.getProject().getId())) {
            throw new IllegalArgumentException("해당 프로젝트의 태그가 아닙니다.");
        }
        tagRepository.delete(tag);
    }
}
