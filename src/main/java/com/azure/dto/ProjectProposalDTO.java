package com.azure.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectProposalDTO {
    private Long id;
    private Long proposerId;
    private String name;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private Long organizationId;
    private Long projectId;
}
