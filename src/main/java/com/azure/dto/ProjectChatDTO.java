package com.azure.dto;

public class ProjectChatDTO {
    private Long projectId;
    private String name;
    private Long channelId; // 없으면 null

    public ProjectChatDTO() {}

    public ProjectChatDTO(Long projectId, String name, Long channelId) {
        this.projectId = projectId;
        this.name = name;
        this.channelId = channelId;
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getChannelId() { return channelId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }
}
