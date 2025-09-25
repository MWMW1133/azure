package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "message_reads")
public class MessageRead {
    @EmbeddedId private MessageReadId id;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("channelId") @JoinColumn(name = "channel_id")
    private ChatChannel channel;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("userId") @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "last_read_message_id")
    private Message lastReadMessage;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
}
