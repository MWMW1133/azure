package com.azure.model.task;

import jakarta.persistence.*;
import lombok.Data;
import com.azure.model.file.FileObject;

@Data
@Entity
@Table(name = "task_attachments")
public class TaskAttachment {
    @EmbeddedId
    private TaskAttachmentId id;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("taskId")
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("fileId")
    @JoinColumn(name = "file_id")
    private FileObject file;
}
