package com.azure.model.chat;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.enums.ChannelType;
import com.azure.model.project.Project;
import com.azure.model.user.User;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "chat_channels")
public class ChatChannel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) @Column(name = "channel_type", length = 16)
    private ChannelType channelType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChannelMember> members = new HashSet<>();
}
