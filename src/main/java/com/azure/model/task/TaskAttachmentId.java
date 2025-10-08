package com.azure.model.task;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class TaskAttachmentId implements Serializable {
    private Long taskId;
    private Long fileId;
}
