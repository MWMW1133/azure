package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "actor_id")
    private User actor;
    @Column(name = "entity_type") private String entityType;
    @Column(name = "entity_id") private Long entityId;
    private String action;
    @Column(name = "diff_json") private String diffJson;
    @Column(name = "created_at") private LocalDateTime createdAt;
}
