package com.azure.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.azure.model.user.User;
// import com.azure.dto.UserRole;

@Data
@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 16)
//    private UserRole role = UserRole.MEMBER;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // 실제로는 organization_members 테이블과 매핑된다고 보면 됨
    @OneToMany(mappedBy = "organization")
    private List<OrganizationMember> members = new ArrayList<>();
}
