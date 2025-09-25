package com.azure.model.project;

import jakarta.persistence.*;
import lombok.Data;
import com.azure.model.user.User;

@Data
@Entity
@Table(name = "project_members")
public class ProjectMember {
    @EmbeddedId
    private ProjectMemberId id;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 50)
    private String role;
}
