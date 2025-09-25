package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.project.ProjectMember;
import com.azure.model.project.ProjectMemberId;
import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    List<ProjectMember> findById_ProjectId(Long projectId);
    List<ProjectMember> findById_UserId(Long userId);
}
