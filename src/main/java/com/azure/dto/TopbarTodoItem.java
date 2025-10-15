// src/main/java/com/azure/dto/TopbarTodoItem.java
package com.azure.dto;

import lombok.Data;

@Data
public class TopbarTodoItem {
    private Long id;
    private String title;
    private String memo;     // description과 동일 의미
    private String startAt;  // ISO-8601 "yyyy-MM-ddTHH:mm:ss"
    private String endAt;    // ISO-8601
    private Boolean allDay;
    private Boolean isDone;
}
