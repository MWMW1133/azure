package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "file_objects")
public class FileObject {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "storage_key") private String storageKey;
    @Column(name = "file_name") private String fileName;
    @Column(name = "mime_type") private String mimeType;
    private Long size;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "uploader_id")
    private User uploader;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "organization_id")
    private Organization organization;
}
