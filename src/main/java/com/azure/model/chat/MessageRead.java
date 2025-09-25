package com.azure.model.chat;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.azure.model.user.User;

@Data
@Entity
@Table(name = "message_reads")
public class MessageRead {
    @EmbeddedId
    private MessageReadId id;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("channelId")
    @JoinColumn(name = "channel_id")
    private ChatChannel channel;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
