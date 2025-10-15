package com.azure.model.workflow;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.project.Project;

@Data
@Entity
@Table(name = "workflows")
public class Workflow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_blocking")
    private Boolean isBlocking;

    @Column(name = "is_terminal")
    private Boolean isTerminal;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "color")
    private String color;

    @PrePersist
    protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Workflow)) return false;
        Workflow other = (Workflow) o;
        // 아직 영속화 전이면 비교 불가 → 객체 동일성 유지
        if (this.id == null || other.id == null) return false;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return (id == null) ? System.identityHashCode(this) : id.hashCode();
    }
}
