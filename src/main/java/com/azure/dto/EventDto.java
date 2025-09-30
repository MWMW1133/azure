package com.azure.dto;

import java.util.Map;
import lombok.Data;

@Data
public class EventDto {
    private String id;
    private String title;
    private String start;
    private String end;
    private boolean allDay;
    private String backgroundColor;
    private Map<String, String> extendedProps; // location, memo, rrule 등 추가 정보
}