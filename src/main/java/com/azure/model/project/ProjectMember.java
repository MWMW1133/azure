package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "project_members")
public class ProjectMember {
    @EmbeddedId private ProjectMemberId id;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("projectId") @JoinColumn(name = "project_id")
    private Project project;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("userId") @JoinColumn(name = "user_id")
    private User user;
    private String role;
}
