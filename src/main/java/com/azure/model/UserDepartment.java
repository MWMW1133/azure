package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="user_departments",
       uniqueConstraints=@UniqueConstraint(name="uq_user_dept", columnNames={"user_id","department_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserDepartment extends BaseTimeEntity {
  @EmbeddedId
  private UserDepartmentId id;

  @MapsId("userId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id")
  private User user;

  @MapsId("departmentId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="department_id")
  private Department department;

  @Column(length=50)
  private String role;
}
