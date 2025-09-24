package com.azure.model;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class TaskAttachmentId implements Serializable {
  private Long taskId;
  private Long fileId;
}
