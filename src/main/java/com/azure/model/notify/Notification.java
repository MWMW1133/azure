package com.azure.model.notify;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.user.User;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 50)
    private String type;

    @Lob @Column(name = "payload")
    private String payload;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
