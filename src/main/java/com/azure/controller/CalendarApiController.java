package com.azure.controller;

import com.azure.dto.EventDto;
import com.azure.model.calendar.PersonalCalendar;
import com.azure.repository.PersonalCalendarRepository;
import com.azure.service.CalendarService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.azure.security.SecurityUtil.requireUserId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarApiController {

    private final CalendarService calendarService;
    private final PersonalCalendarRepository personalCalendarRepository;
    private final HttpSession httpSession;


    // ───────────── 조회 ─────────────
    @GetMapping("/events")
    public List<EventDto> getEvents(@RequestParam(required = false) String start,
                                    @RequestParam(required = false) String end) {
        Long uid = requireUserId(httpSession);   // ← 여기만 바뀜

        LocalDateTime from = (start != null && !start.isBlank())
                ? parseFlexibleForAllDay(start, false, false)
                : LocalDate.now().minusMonths(1).atStartOfDay();

        LocalDateTime to = (end != null && !end.isBlank())
                ? parseFlexibleForAllDay(end, true, false)
                : LocalDate.now().plusMonths(2).atTime(23, 59, 59);

        List<PersonalCalendar> rows =
                personalCalendarRepository.findByCreatedBy_IdAndStartAtBetween(uid, from, to);

        return rows.stream().map(this::toEventDto).collect(Collectors.toList());
    }

    // ───────────── 생성 ─────────────
    @PostMapping("/events")
    public EventDto createEvent(@RequestBody EventDto in) {
         Long uid = requireUserId(httpSession);

        boolean allDay = in.isAllDay();
        LocalDateTime startAt = parseFlexibleForAllDay(in.getStart(), false, allDay);
        LocalDateTime endAt   = parseFlexibleForAllDay(in.getEnd(),   true,  allDay);

        // rrule은 top-level 우선, 없으면 extendedProps에서
        String rrule = nvl(in.getRrule(), ext(in, "rrule"));
        String memo = ext(in, "memo");
        String location = ext(in, "location");

        PersonalCalendar saved = calendarService.schedulePersonalEvent(
                uid,
                nvl(in.getTitle(), ""),
                memo,
                startAt,
                endAt,
                allDay,
                rrule,
                location
        );

        // 색상 컬럼 반영
        if (in.getBackgroundColor() != null && !in.getBackgroundColor().isBlank()) {
            saved.setColor(in.getBackgroundColor());
            saved = personalCalendarRepository.save(saved);
        }

        // 참고: JS가 보내는 "reminder"는 DTO에 없어도 스프링이 무시(기본 설정). 필요 시 별도 처리 추가.

        return toEventDto(saved);
    }

    // ───────────── 수정 ─────────────
    @PutMapping("/events/{id}")
    public EventDto updateEvent(@PathVariable("id") String id, @RequestBody EventDto in) {
        Long eid = Long.valueOf(id);
        PersonalCalendar e = personalCalendarRepository.findById(eid)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));

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

    // ───────────── 삭제 ─────────────
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") String id) {
        calendarService.deletePersonalEvent(Long.valueOf(id));
        return ResponseEntity.ok().build();
    }

    // ───────────── 변환/유틸 ─────────────
    private EventDto toEventDto(PersonalCalendar e) {
        EventDto dto = new EventDto();
        dto.setId(String.valueOf(e.getId()));
        dto.setTitle(e.getTitle());
        dto.setStart(e.getStartAt().toString()); // "yyyy-MM-ddTHH:mm:ss"
        dto.setEnd(e.getEndAt().toString());
        dto.setAllDay(Boolean.TRUE.equals(e.getAllDay()));
        dto.setBackgroundColor((e.getColor() == null || e.getColor().isBlank()) ? "blue" : e.getColor());

        // rrule은 top-level과 extendedProps에 함께 실어줌(플러그인/프론트 호환성 ↑)
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

    /** s1이 비어있으면 s2 반환 */
    private static String nvl(String s1, String s2) {
        return (s1 != null && !s1.isBlank()) ? s1 : s2;
    }

    /**
     * "yyyy-MM-dd" 또는 "yyyy-MM-ddTHH:mm[:ss]" 모두 허용.
     * allDay=true면 시작은 00:00, 종료는 23:59로 보정.
     */
    private static LocalDateTime parseFlexibleForAllDay(String s, boolean end, boolean allDay) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        if (t.length() == 10) { // yyyy-MM-dd
            LocalDate d = LocalDate.parse(t);
            return end ? d.atTime(23, 59) : d.atStartOfDay();
        }
        String iso = t.replace(' ', 'T');
        if (iso.length() == 16) iso = iso + ":00"; // 초 생략 보정
        return LocalDateTime.parse(iso.substring(0, 19));
    }
}
