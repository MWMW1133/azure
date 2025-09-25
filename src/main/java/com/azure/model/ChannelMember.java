package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "channel_members")
public class ChannelMember {
    @EmbeddedId private ChannelMemberId id;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("channelId") @JoinColumn(name = "channel_id")
    private ChatChannel channel;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("userId") @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "joined_at") private LocalDateTime joinedAt;
}
