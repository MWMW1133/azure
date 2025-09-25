package com.azure.model.task;

import jakarta.persistence.*;
import lombok.Data;
import com.azure.model.enums.TaskDependencyType;

@Data
@Entity
@Table(name = "task_dependencies")
public class TaskDependency {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predecessor_id")
    private Task predecessor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "successor_id")
    private Task successor;

    @Enumerated(EnumType.STRING) @Column(length = 16)
    private TaskDependencyType type;

    @Column(name = "lag_days")
    private Integer lagDays;
}
