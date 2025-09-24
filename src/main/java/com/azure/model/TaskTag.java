package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="task_tags",
  uniqueConstraints=@UniqueConstraint(name="uq_task_tag", columnNames={"task_id","tag_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskTag {
  @EmbeddedId
  private TaskTagId id;

  @MapsId("taskId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="task_id")
  private Task task;

  @MapsId("tagId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="tag_id")
  private Tag tag;
}
