package com.azure.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

public class TaskTagDTO {
    private Long taskId;
    private Long tagId;


    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
