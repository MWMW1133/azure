package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="project_members",
       uniqueConstraints=@UniqueConstraint(name="uq_project_member", columnNames={"project_id","user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectMember extends BaseTimeEntity {
  @EmbeddedId
  private ProjectMemberId id;

  @MapsId("projectId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_id")
  private Project project;

  @MapsId("userId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id")
  private User user;

  @Column(length=50)
  private String role;
}
