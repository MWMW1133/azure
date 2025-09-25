package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "chat_channels")
public class ChatChannel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "channel_type") private String channelType; // ENUM
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_id")
    private Project project;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by")
    private User createdBy;
    @Column(name = "created_at") private LocalDateTime createdAt;
}
