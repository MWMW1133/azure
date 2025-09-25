package com.azure.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.enums.OrganizationRole;

@Data
@Entity
@Table(name = "organizations")
public class Organization {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.azure.model.user.User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 16)
    private OrganizationRole role = OrganizationRole.MEMBER;
}
