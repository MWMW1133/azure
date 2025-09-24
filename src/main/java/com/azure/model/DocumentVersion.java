package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="document_versions",
  uniqueConstraints=@UniqueConstraint(name="uq_doc_version", columnNames={"document_id","version_no"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentVersion extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="document_id", nullable=false)
  private Document document;

  @Column(name="version_no", nullable=false)
  private Integer versionNo;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="file_id")
  private FileObject file;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="author_id")
  private User author;
}
