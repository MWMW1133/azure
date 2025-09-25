package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "task_dependencies")
public class TaskDependency {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "predecessor_id")
    private Task predecessor;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "successor_id")
    private Task successor;
    private String type; // ENUM('BLOCKS','RELATES','DUPLICATES')
    @Column(name = "lag_days") private Integer lagDays;
}
