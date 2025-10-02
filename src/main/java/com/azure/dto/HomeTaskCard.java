package com.azure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HomeTaskCard {
    private Long id;
    private String title;
    private String assigneeName;
    private String assigneeImage;
    private String startedAt;
    private String dueDate;
    private String status;      // in-progress, completed...
    private String priority;    // high, normal, …
    private int processPct;     // 0~100
    private boolean hasFile;
    private String updatedAt;
}
