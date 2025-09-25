package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "workflows")
public class Workflow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_id")
    private Project project;
    private String name;
    @Column(name = "sort_order") private Integer sortOrder;
    @Column(name = "is_blocking") private Boolean isBlocking;
    @Column(name = "is_terminal") private Boolean isTerminal;
    @Column(name = "is_default") private Boolean isDefault;
    @Column(name = "created_at") private LocalDateTime createdAt;
}
