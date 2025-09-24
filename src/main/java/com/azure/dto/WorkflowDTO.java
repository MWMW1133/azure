package com.azure.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

public class WorkflowDTO {
    private Long id;
    private Long projectId;
    private String name;
    private Integer sortOrder;
    private Boolean isBlocking;
    private Boolean isTerminal;
    private Boolean isDefault;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsBlocking() {
        return isBlocking;
    }

    public void setIsBlocking(Boolean isBlocking) {
        this.isBlocking = isBlocking;
    }

    public Boolean getIsTerminal() {
        return isTerminal;
    }

    public void setIsTerminal(Boolean isTerminal) {
        this.isTerminal = isTerminal;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
