// src/main/java/com/azure/controller/CalendarApiController.java
package com.azure.controller;

import com.azure.dto.EventDto;
import com.azure.dto.TopbarTodoItem;
import com.azure.model.calendar.PersonalCalendar;
import com.azure.repository.PersonalCalendarRepository;
import com.azure.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarApiController {

    private final CalendarService calendarService;
    private final PersonalCalendarRepository personalCalendarRepository;

    /* ========== FullCalendar: 개인 일정 CRUD ========== */

    @GetMapping("/events")
    public List<EventDto> getEvents(
            @ModelAttribute("currentUserId") Long uid,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

        LocalDateTime from = (start != null && !start.isBlank())
                ? parseFlexibleForAllDay(start, false, false)
                : LocalDate.now().minusMonths(1).atStartOfDay();

        LocalDateTime to = (end != null && !end.isBlank())
                ? parseFlexibleForAllDay(end, true, false)
                : LocalDate.now().plusMonths(2).atTime(23, 59, 59);

        var rows = personalCalendarRepository.findByCreatedBy_IdAndStartAtBetween(uid, from, to);
        return rows.stream().map(this::toEventDto).collect(Collectors.toList());
    }

    @PostMapping("/events")
    public EventDto createEvent(@ModelAttribute("currentUserId") Long uid,
                                @RequestBody EventDto in) {
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

        boolean allDay = in.isAllDay();
        LocalDateTime startAt = parseFlexibleForAllDay(in.getStart(), false, allDay);
        LocalDateTime endAt   = parseFlexibleForAllDay(in.getEnd(),   true,  allDay);

        String rrule    = nvl(in.getRrule(), ext(in, "rrule"));
        String memo     = ext(in, "memo");
        String location = ext(in, "location");

        PersonalCalendar saved = calendarService.schedulePersonalEvent(
                uid, nvl(in.getTitle(), ""), memo, startAt, endAt, allDay, rrule, location
        );

        if (in.getBackgroundColor() != null && !in.getBackgroundColor().isBlank()) {
            saved.setColor(in.getBackgroundColor());
            saved = personalCalendarRepository.save(saved);
        }
        return toEventDto(saved);
    }

    @PutMapping("/events/{id}")
    public EventDto updateEvent(@ModelAttribute("currentUserId") Long uid,
                                @PathVariable("id") String id,
                                @RequestBody EventDto in) {
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

        Long eid = Long.valueOf(id);
        PersonalCalendar e = personalCalendarRepository.findById(eid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + id));

        boolean allDay = in.isAllDay();
        LocalDateTime startAt = parseFlexibleForAllDay(in.getStart(), false, allDay);
        LocalDateTime endAt   = parseFlexibleForAllDay(in.getEnd(),   true,  allDay);

        e.setTitle(nvl(in.getTitle(), ""));
        e.setDescription(ext(in, "memo"));
        e.setLocation(ext(in, "location"));
        e.setRrule(nvl(in.getRrule(), ext(in, "rrule")));
        e.setAllDay(allDay);
        e.setStartAt(startAt);
        e.setEndAt(endAt);

        if (in.getBackgroundColor() != null && !in.getBackgroundColor().isBlank()) {
            e.setColor(in.getBackgroundColor());
        }

        e = personalCalendarRepository.save(e);
        return toEventDto(e);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@ModelAttribute("currentUserId") Long uid,
                                            @PathVariable("id") String id) {
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        calendarService.deletePersonalEvent(Long.valueOf(id));
        return ResponseEntity.ok().build();
    }

    /* ========== Topbar To-do: 홈과 동일 ‘겹침’ 기준 ========== */

    @GetMapping("/today")
    public List<TopbarTodoItem> listToday(@ModelAttribute("currentUserId") Long uid) {
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end   = today.plusDays(1).atStartOfDay(); // [start, end) 구간

        var events = calendarService.listPersonalEventsBetween(uid, start, end);

        return events.stream().map(e -> {
            TopbarTodoItem t = new TopbarTodoItem();
            t.setId(e.getId());
            t.setTitle(e.getTitle());
            t.setMemo(e.getDescription());
            t.setStartAt(e.getStartAt() != null ? e.getStartAt().toString() : null);
            t.setEndAt(e.getEndAt() != null ? e.getEndAt().toString() : null);
            t.setAllDay(Boolean.TRUE.equals(e.getAllDay()));
            t.setIsDone(Boolean.TRUE.equals(e.getIsDone()));
            return t;
        }).toList();
    }

    @PostMapping("/{id}/toggle-done")
    public TopbarTodoItem toggleDone(@ModelAttribute("currentUserId") Long uid,
                                     @PathVariable Long id,
                                     @RequestParam boolean done) {
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

        var updated = calendarService.setPersonalEventDone(id, done);

        TopbarTodoItem t = new TopbarTodoItem();
        t.setId(updated.getId());
        t.setTitle(updated.getTitle());
        t.setMemo(updated.getDescription());
        t.setStartAt(updated.getStartAt() != null ? updated.getStartAt().toString() : null);
        t.setEndAt(updated.getEndAt() != null ? updated.getEndAt().toString() : null);
        t.setAllDay(Boolean.TRUE.equals(updated.getAllDay()));
        t.setIsDone(Boolean.TRUE.equals(updated.getIsDone()));
        return t;
    }

    /* ========== 내부 유틸 ========== */

    private EventDto toEventDto(PersonalCalendar e) {
        EventDto dto = new EventDto();
        dto.setId(String.valueOf(e.getId()));
        dto.setTitle(e.getTitle());
        dto.setStart(e.getStartAt().toString());
        dto.setEnd(e.getEndAt().toString());
        dto.setAllDay(Boolean.TRUE.equals(e.getAllDay()));
        dto.setBackgroundColor((e.getColor() == null || e.getColor().isBlank()) ? "blue" : e.getColor());
        dto.setRrule(e.getRrule());

        Map<String, String> ext = new HashMap<>();
        if (e.getLocation() != null)    ext.put("location", e.getLocation());
        if (e.getDescription() != null) ext.put("memo", e.getDescription());
        if (e.getRrule() != null)       ext.put("rrule", e.getRrule());
        dto.setExtendedProps(ext);
        return dto;
    }

    private static String ext(EventDto in, String key) {
        Map<String, String> m = in.getExtendedProps();
        return (m == null) ? null : m.get(key);
    }

    private static String nvl(String s1, String s2) {
        return (s1 != null && !s1.isBlank()) ? s1 : s2;
    }

    /** "yyyy-MM-dd" 또는 "yyyy-MM-ddTHH:mm[:ss]" 허용. */
    private static LocalDateTime parseFlexibleForAllDay(String s, boolean end, boolean allDay) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        if (t.length() == 10) { // yyyy-MM-dd
            LocalDate d = LocalDate.parse(t);
            return end ? d.atTime(23, 59) : d.atStartOfDay();
        }
        String iso = t.replace(' ', 'T');
        if (iso.length() == 16) iso = iso + ":00";
        return LocalDateTime.parse(iso.substring(0, 19));
    }

    private static String fmtTime(LocalDateTime start, LocalDateTime end, DateTimeFormatter f) {
        if (start == null || end == null) return "";
        return start.format(f) + " - " + end.format(f);
    }
}
