package com.azure.model.project;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.enums.ProposalStatus;
import com.azure.model.user.User;
import com.azure.model.Organization;

@Data
@Entity
@Table(name = "project_proposals")
public class ProjectProposal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposer_id")
    private User proposer;

    @Column(nullable = false, length = 255)
    private String name;

    @Lob @Column
    private String description;

    @Enumerated(EnumType.STRING) @Column(length = 16)
    private ProposalStatus status = ProposalStatus.PENDING;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;
}
