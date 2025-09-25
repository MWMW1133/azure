package com.azure.model.user;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.Organization;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 255)
    private String name;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;
}
