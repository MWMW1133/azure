package com.azure;

import com.azure.model.notify.Notification;
import com.azure.model.project.Project;
import com.azure.model.project.ProjectMember;
import com.azure.model.project.ProjectMemberId;
import com.azure.model.user.User;
import com.azure.repository.NotificationRepository;
import com.azure.repository.ProjectMemberRepository;
import com.azure.repository.ProjectRepository;
import com.azure.repository.UserRepository;
import com.azure.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProjectServiceIntegrationTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void removeMember_shouldDeleteAndCreateNotification() {
        // given
        User owner = new User();
        owner.setName("owner");
        userRepository.save(owner);

        User member = new User();
        member.setName("member");
        userRepository.save(member);

        Project project = new Project();
        project.setName("Demo Project");
        project.setOwner(owner);
        project.setOrganization(null); // 조직 필수라면 실제 엔티티 저장 후 설정
        projectRepository.save(project);

        ProjectMember pm = new ProjectMember();
        pm.setId(new ProjectMemberId(project.getId(), member.getId()));
        pm.setProject(project);
        pm.setUser(member);
        pm.setRole("MEMBER");
        projectMemberRepository.save(pm);

        // when
        projectService.removeMember(project.getId(), member.getId(), owner.getId());

        // then 1) 멤버 삭제 확인
        assertThat(projectMemberRepository.existsById(new ProjectMemberId(project.getId(), member.getId())))
                .isFalse();

        // then 2) 알림 생성 확인
        Notification notif = notificationRepository.findAll().stream()
                .filter(n -> n.getUser().getId().equals(member.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("알림이 생성되지 않았습니다."));

        assertThat(notif.getType()).isEqualTo("PROJECT_MEMBER_REMOVED");
        assertThat(notif.getPayload()).contains("projectId", "removedByUserId");
    }
}

