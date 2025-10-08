package com.azure.model.project;

import com.azure.model.Organization;
import com.azure.model.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "project_proposals")
public class ProjectProposal {

    public enum Status {
        APPROVED, PENDING, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 제안자 (users.id FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposer_id", nullable = false)
    private User proposer;

    /** 조직 (organizations.id FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /** 승인 후 연결된 프로젝트 (projects.id FK) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", unique = true)
    private Project project;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** 제안 상태 */
    @Enumerated(EnumType.STRING)
    private Status status; // APPROVED/PENDING/REJECTED

    /** 예상 시작일 */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** 예상 마감일 */
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
