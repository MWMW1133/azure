package com.azure.model.project;

import jakarta.persistence.*;
import lombok.Data;
import com.azure.model.user.User;
import com.azure.dto.UserRole;

@Data
@Entity
@Table(name = "project_members")
public class ProjectMember {
    @EmbeddedId
    private ProjectMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING) // Enum → 문자열("LEADER", "MEMBER")로 저장
    @Column(nullable = false, length = 20)
    private UserRole role;
}
