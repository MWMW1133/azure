package com.azure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectProposalDto {
    private Long id;
    private Long proposerId;
    private Long organizationId;
    private Long projectId;
    private String title; 
    private String proposer; 
    private String proposerAvatarUrl;
    private String description;
    private String status;
    private LocalDate createdAt;
    private LocalDate startDate;
    private LocalDate endDate;
}