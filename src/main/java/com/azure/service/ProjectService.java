package com.azure.service;

import com.azure.dto.UserRole;
import com.azure.model.project.Project;
import com.azure.model.project.ProjectMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 프로젝트 생성/수정/삭제 및 멤버십 관리 기능을 제공한다.
 */
public interface ProjectService {
    /** ID로 프로젝트 조회. 없으면 NotFoundException. */
    Project get(Long id);

    /** 특정 사용자가 속한 모든 회사의 프로젝트 목록(페이징). */
    Page<Project> listByUser(Long userId, Pageable pageable);

    /** 조직 소속으로 프로젝트 생성(소유자 지정). */
    Project create(Long organizationId, Long ownerId, String name, String description);

    /** 기본 메타데이터(이름/설명) 수정. */
    Project update(Long projectId, String name, String description);

    /** 프로젝트 삭제(FK 제약/ON DELETE는 DDL에 따름). */
    void delete(Long projectId);

    /** 멤버 추가(역할 라벨: 예 "OWNER", "MEMBER"). */
    ProjectMember addMember(Long projectId, Long userId, UserRole role);

    /** 멤버 제거(존재하지 않으면 무시). */
    void removeMember(Long projectId, Long userId, Long removedByUserId);
    /** 특정 사용자가 특정 프로젝트의 멤버인지 여부 확인 */
    boolean existsMember(Long projectId, Long userId); 
    /** 특정 프로젝트의 멤버 목록 조회(페이징) */
    Page<ProjectMember> listMembers(Long projectId, Pageable pageable);
}
