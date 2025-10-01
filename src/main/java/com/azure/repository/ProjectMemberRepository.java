package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.azure.model.project.ProjectMember;
import com.azure.model.project.ProjectMemberId;
import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    List<ProjectMember> findById_ProjectId(Long projectId);
    List<ProjectMember> findById_UserId(Long userId);
    @Query("select pm.user.id from ProjectMember pm where pm.project.id = :projectId")
    List<Long> findUserIdsByProjectId(Long projectId);

    // 프로젝트 + 유저 ID 조합으로 멤버 조회
    Optional<ProjectMember> findById_ProjectIdAndId_UserId(Long projectId, Long userId);
    
    // 특정 프로젝트에 특정 사용자가 속해있는지 확인
    boolean existsById_ProjectIdAndId_UserId(Long projectId, Long userId);
}
