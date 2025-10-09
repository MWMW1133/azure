package com.azure.model.user;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.Organization;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;   

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 255)
    private String name;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    // ✅ WorkStatus Enum 매핑
    public enum WorkStatus {
        WORKING,     // 근무중 (디폴트)
        LONG_LEAVE,  // 장기 휴가
        ANNUAL,      // 연차
        HALF_DAY     // 반차
    }

    @Enumerated(EnumType.STRING) // DB에 문자열로 저장 ("WORKING", "LONG_LEAVE"...)
    @Column(name = "work_status", nullable = false)
    private WorkStatus workStatus = WorkStatus.WORKING;  // 기본값: 근무중

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    
    @PrePersist
    protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    }
    
}
