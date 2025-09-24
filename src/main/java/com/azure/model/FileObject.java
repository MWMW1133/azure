package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="file_objects", indexes=@Index(name="idx_file_uploader", columnList="uploader_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FileObject extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="uploader_id")
  private User uploader;

  @Column(name="file_name", nullable=false, length=255)
  private String fileName;

  @Column(name="mime_type", length=120)
  private String mimeType;

  @Column(name="size")
  private Long size;

  @Column(name="storage_url", length=500)
  private String storageUrl;
}
