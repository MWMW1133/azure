package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "task_attachments")
public class TaskAttachment {
    @EmbeddedId private TaskAttachmentId id;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("taskId") @JoinColumn(name = "task_id")
    private Task task;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("fileId") @JoinColumn(name = "file_id")
    private FileObject file;
}
