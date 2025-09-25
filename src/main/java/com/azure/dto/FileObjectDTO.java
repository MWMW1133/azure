package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileObjectDTO {
    private Long id;
    private String storageKey;
    private String fileName;
    private String mimeType;
    private Long size;
    private Long uploaderId;
    private LocalDateTime createdAt;
    private Long organizationId;
}
