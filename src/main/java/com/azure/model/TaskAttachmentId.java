package com.azure.model;


import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Data;

@Data @Embeddable
public class TaskAttachmentId implements Serializable {
    private Long taskId;
    private Long fileId;
}
