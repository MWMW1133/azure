package com.azure.dto;


public class WorkflowDTO {
    private int id;
    private int projectId;
    private String name;
    private int sortOrder;
    private boolean isBlocking;
    private boolean isTerminal;
    private boolean isDefault;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean getIsBlocking() {
        return isBlocking;
    }

    public void setIsBlocking(boolean isBlocking) {
        this.isBlocking = isBlocking;
    }

    public boolean getIsTerminal() {
        return isTerminal;
    }

    public void setIsTerminal(boolean isTerminal) {
        this.isTerminal = isTerminal;
    }

    public boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
