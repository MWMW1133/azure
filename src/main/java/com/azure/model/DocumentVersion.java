package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "document_versions")
public class DocumentVersion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "document_id")
    private Document document;
    @Column(name = "version_num") private Integer versionNum;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "file_id")
    private FileObject file;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id")
    private User author;
    @Column(name = "created_at") private LocalDateTime createdAt;
}
