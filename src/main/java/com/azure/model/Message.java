package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "messages")
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "channel_id")
    private ChatChannel channel;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id")
    private User author;
    @Column(columnDefinition = "TEXT") private String body;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "file_id")
    private FileObject file;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reply_to_id")
    private Message replyTo;
    @Column(name = "created_at") private LocalDateTime createdAt;
}
