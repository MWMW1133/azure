package com.azure.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class EventDto {
    private String id;
    private String title;
    private String start;
    private String rrule;
    private String end;
    private boolean allDay;
    private String backgroundColor;
    private Map<String, String> extendedProps; // location, memo, rrule 등

    // ✅ 선택: 프론트에서 참석자 뱃지/체크박스로 쓰고 싶을 때
    private List<Long> attendeeIds;
}
