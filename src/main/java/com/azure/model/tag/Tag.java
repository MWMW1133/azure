package com.azure.model.tag;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.project.Project;

@Data
@Entity
@Table(name = "tags")
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 30)
    private String color;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
