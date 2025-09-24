package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="channel_members",
  uniqueConstraints=@UniqueConstraint(name="uq_channel_member", columnNames={"channel_id","user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChannelMember extends BaseTimeEntity {
  @EmbeddedId
  private ChannelMemberId id;

  @MapsId("channelId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="channel_id")
  private ChatChannel channel;

  @MapsId("userId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id")
  private User user;

  @Column(length=50)
  private String role;
}
