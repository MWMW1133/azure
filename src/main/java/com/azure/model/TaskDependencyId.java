package com.azure.model;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class TaskDependencyId implements Serializable {
  private Long predecessorId;
  private Long successorId;
}
