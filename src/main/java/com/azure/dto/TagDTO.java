package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TagDTO {
    private Long id;
    private Long projectId;
    private String name;
    private String color;
    private LocalDateTime createdAt;
}
