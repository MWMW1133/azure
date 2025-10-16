package com.azure.service; // (패키지 경로는 실제 프로젝트에 맞게 수정)

import com.azure.model.project.ProjectInvitation;
import com.azure.model.enums.InvitationStatus;
import com.azure.repository.ProjectInvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectInvitationService {

    private final ProjectInvitationRepository invitationRepository;

    // 특정 프로젝트에 초대된 (아직 수락/거절 안 한) 사용자들의 ID 목록을 반환
    public List<Long> getPendingInvitedUserIds(Long projectId) {
        List<ProjectInvitation> invitations =
                invitationRepository.findByProjectIdAndStatus(projectId, InvitationStatus.PENDING);

        return invitations.stream()
                .map(invitation -> invitation.getUser().getId())
                .toList();
    }
}