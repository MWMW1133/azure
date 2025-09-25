package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentDTO {
    private Long id;
    private Long projectId;
    private String title;
    private String templateKey;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
