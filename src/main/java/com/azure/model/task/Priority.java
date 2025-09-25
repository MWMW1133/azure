package com.azure.model.task;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "priorities")
public class Priority {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "smallint unsigned")
    private Integer id;

    @Column(nullable = false, length = 32)
    private String name;

    @Column(nullable = false)
    private Integer level;
}
