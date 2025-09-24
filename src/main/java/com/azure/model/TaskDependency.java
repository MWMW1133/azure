package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="task_dependencies",
  uniqueConstraints=@UniqueConstraint(name="uq_task_dep", columnNames={"predecessor_id","successor_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskDependency {
  @EmbeddedId
  private TaskDependencyId id;

  @MapsId("predecessorId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="predecessor_id")
  private Task predecessor;

  @MapsId("successorId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="successor_id")
  private Task successor;
}
