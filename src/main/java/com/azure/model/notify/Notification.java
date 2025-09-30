// Notification.java
package com.azure.model.notify;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.user.User;

@Data
@Entity @Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 50)  private String type;     // e.g. PROJECT_EVENT_REMINDER
    @Lob                   private String payload; // 간단 JSON/문자
    @Column(name="is_read") private Boolean read;  // TINYINT(1)
    @Column(name="created_at", insertable=false, updatable=false)
    private LocalDateTime createdAt;
}
