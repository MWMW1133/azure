package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentVersionDTO {
    private Long id;
    private Long documentId;
    private Integer versionNum;
    private Long fileId;
    private Long authorId;
    private LocalDateTime createdAt;
}
