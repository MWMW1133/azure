package com.azure.repository; // (패키지 경로는 실제 프로젝트에 맞게 수정)

import com.azure.model.project.ProjectInvitation;
import com.azure.model.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {

    // 특정 프로젝트의 특정 상태를 가진 초대 목록을 찾는 메서드
    List<ProjectInvitation> findByProjectIdAndStatus(Long projectId, InvitationStatus status);
}