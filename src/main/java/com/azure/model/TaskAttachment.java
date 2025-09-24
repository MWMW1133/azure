package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="task_attachments",
  uniqueConstraints=@UniqueConstraint(name="uq_task_file", columnNames={"task_id","file_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskAttachment {
  @EmbeddedId
  private TaskAttachmentId id;

  @MapsId("taskId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="task_id")
  private Task task;

  @MapsId("fileId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="file_id")
  private FileObject file;
}
