package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "user_departments")
public class UserDepartment {
    @EmbeddedId private UserDepartmentId id;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("userId") @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("departmentId") @JoinColumn(name = "department_id")
    private Department department;
    @Column(name = "role_in_dept") private String roleInDept;
}
