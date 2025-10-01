package com.azure.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.azure.dto.EventDto;

@RestController 
@RequestMapping("/api/calendar") 
public class CalendarApiController {

    // private final CalendarService calendarService;

    // 모든 일정 조회
    @GetMapping("/events")
    public List<EventDto> getAllEvents() {
        
        // 더미 데이터 반환
        List<EventDto> events = new ArrayList<>();
        EventDto event1 = new EventDto();
        event1.setId("1");
        event1.setTitle("팀 전체 회의");
        event1.setStart("2025-09-29T10:30:00");
        event1.setEnd("2025-09-29T12:00:00");
        event1.setBackgroundColor("blue");
        event1.setExtendedProps(Map.of("location", "3번 회의실"));
        
        EventDto event2 = new EventDto();
        event2.setId("2");
        event2.setTitle("디자인 리뷰");
        event2.setStart("2025-09-30");
        event2.setAllDay(true);
        event2.setBackgroundColor("green");
        event2.setRrule("FREQ=WEEKLY;BYDAY=TU");
        event2.setExtendedProps(Map.of("rrule", "FREQ=WEEKLY;BYDAY=TU"));

        events.add(event1);
        events.add(event2);
        
        return events;
    }

    // 새 일정 추가
    @PostMapping("/events")
    public EventDto createEvent(@RequestBody EventDto newEvent) {  
        // 지금은 ID만 부여해서 그대로 반환
        newEvent.setId(String.valueOf(System.currentTimeMillis()));
        
        return newEvent;
    }

    // 일정 수정
    @PutMapping("/events/{id}")
    public EventDto updateEvent(@PathVariable("id") String id, @RequestBody EventDto updatedEvent) {
    
        // 수정 로직 필요
        return updatedEvent;
    }
    
    // 일정 삭제
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") String id) {
        
        // 지금은 성공했다는 응답만 반환
        return ResponseEntity.ok().build();
    }
}