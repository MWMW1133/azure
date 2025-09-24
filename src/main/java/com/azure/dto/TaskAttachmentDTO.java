package com.azure.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

public class TaskAttachmentDTO {
    private Long taskId;
    private Long fileId;


    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }
}
