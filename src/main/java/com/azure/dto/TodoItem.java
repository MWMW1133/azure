package com.azure.dto;

public class TodoItem {
    private String title;
    private String time;
    private String status;
    private String description;

    public TodoItem(String title, String time, String status, String description) {
        this.title = title;
        this.time = time;
        this.status = status;
        this.description = description;
    }
    // Getter 메소드들...
    public String getTitle() { return title; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public String getdescription() { return description; }
}