package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "organizations")
public class Organization {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "created_at") private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY) // creator/owner
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "role")
    private String role; // MANAGER / MEMBER
}
