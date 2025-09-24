package com.azure.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="messages_reads",
  uniqueConstraints=@UniqueConstraint(name="uq_msgread", columnNames={"channel_id","user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageRead extends BaseTimeEntity {
  @EmbeddedId
  private MessageReadId id;

  @MapsId("channelId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="channel_id")
  private ChatChannel channel;

  @MapsId("userId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id")
  private User user;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="last_read_message_id")
  private Message lastRead;

  @Column(name="updated_at")
  private LocalDateTime lastUpdatedAt; // mirrors BaseTimeEntity.updatedAt but explicit if needed
}
