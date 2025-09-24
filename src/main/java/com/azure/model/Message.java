package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="messages", indexes=@Index(name="idx_msg_channel_created", columnList="channel_id,created_at"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="channel_id", nullable=false)
  private ChatChannel channel;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="author_id", nullable=false)
  private User author;

  @Lob @Column(columnDefinition="longtext")
  private String content;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="file_id")
  private FileObject file;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="reply_to_id")
  private Message replyTo;
}
