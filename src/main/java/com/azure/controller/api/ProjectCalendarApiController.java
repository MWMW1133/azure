package com.azure.controller.api;

import com.azure.dto.EventDto;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.task.Task;
import com.azure.model.user.User;
import com.azure.repository.EventAttendeeRepository;
import com.azure.repository.ProjectCalendarRepository;
import com.azure.repository.TaskRepository;
import com.azure.service.CalendarService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/projects/{projectId}/calendar")
@RequiredArgsConstructor
public class ProjectCalendarApiController {

    private final CalendarService calendarService;
    private final ProjectCalendarRepository projectCalendarRepository;
    private final TaskRepository taskRepository;
    private final EventAttendeeRepository eventAttendeeRepository;

    // ───────── 공통 유틸 ─────────
    private Long currentUserId(HttpSession session) {
        User u = (User) session.getAttribute("loginUser");
        return (u != null) ? u.getId() : null;
    }

    private static String ext(EventDto in, String key) {
        Map<String, String> m = in.getExtendedProps();
        return (m == null) ? null : m.get(key);
    }
    private static String nvl(String a, String b) { return (a != null && !a.isBlank()) ? a : b; }
    private static Long parseLongOrNull(String s) {
        try { return (s == null || s.isBlank()) ? null : Long.valueOf(s.trim()); }
        catch (Exception e) { return null; }
    }

    /** "yyyy-MM-dd" 또는 "yyyy-MM-ddTHH:mm[:ss]" 허용. allDay면 00:00/23:59 보정 */
    private static LocalDateTime parseFlexible(String s, boolean end, boolean allDay, LocalDateTime fallback) {
        if (s == null || s.isBlank()) return fallback;
        String t = s.trim();
        if (t.length() == 10) {
            var d = LocalDate.parse(t);
            return end ? d.atTime(23, 59) : d.atStartOfDay();
        }
        String iso = t.replace(' ', 'T');
        if (iso.length() == 16) iso = iso + ":00";
        return LocalDateTime.parse(iso.substring(0, 19));
    }

    private EventDto toEventDto(ProjectCalendar e) {
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
        if (e.getRelatedTask() != null) ext.put("relatedTaskId", String.valueOf(e.getRelatedTask().getId()));
        dto.setExtendedProps(ext);

        // 참석자 ID 목록(이 DTO는 화면 렌더용 참고값)
        List<Long> attendeeIds = eventAttendeeRepository.findByEvent_Id(e.getId())
                .stream().map(a -> a.getUser().getId()).toList();
        dto.setAttendeeIds(attendeeIds);

        return dto;
    }

    /** 태스크 → 읽기전용 종일 이벤트 */
    private EventDto toTaskEventDto(Task t) {
        EventDto dto = new EventDto();
        dto.setId("T-" + t.getId());
        dto.setTitle("[태스크] " + nvl(t.getTitle(), ""));
        dto.setStart(t.getStartDate().atStartOfDay().toString());
        dto.setEnd(t.getDueDate().atTime(23, 59).toString());
        dto.setAllDay(true);
        dto.setBackgroundColor("gray");
        Map<String,String> ext = new HashMap<>();
        ext.put("relatedTaskId", String.valueOf(t.getId()));
        dto.setExtendedProps(ext);
        return dto;
    }

    // ───────── 이벤트 목록 ─────────
    @GetMapping("/events")
    public List<EventDto> getEvents(@PathVariable Long projectId,
                                    @RequestParam(required = false) String start,
                                    @RequestParam(required = false) String end) {

        LocalDateTime from = parseFlexible(start, false, false,
                LocalDate.now().minusMonths(1).atStartOfDay());
        LocalDateTime to   = parseFlexible(end,   true,  false,
                LocalDate.now().plusMonths(2).atTime(23,59,59));

        List<ProjectCalendar> events =
                projectCalendarRepository.findByProjectIdAndRangeOverlap(projectId, from, to);

        List<Task> tasks = taskRepository.findByProjectAndDateRangeOverlap(
                projectId, from.toLocalDate(), to.toLocalDate());

        List<EventDto> result = new ArrayList<>();
        for (ProjectCalendar e : events) result.add(toEventDto(e));
        for (Task t : tasks) {
            if (t.getStartDate()!=null && t.getDueDate()!=null) result.add(toTaskEventDto(t));
        }
        return result;
    }

    // ───────── 생성 ─────────
    @PostMapping("/events")
    public EventDto create(HttpSession session,
                           @PathVariable Long projectId,
                           @RequestBody EventDto in) {

        Long uid = currentUserId(session);
        if (uid == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        boolean allDay = in.isAllDay();
        var startAt = parseFlexible(in.getStart(), false, allDay, null);
        var endAt   = parseFlexible(in.getEnd(),   true,  allDay, null);

        ProjectCalendar saved = calendarService.scheduleProjectEvent(
                projectId,
                nvl(in.getTitle(), ""),
                ext(in, "memo"),
                startAt, endAt,
                allDay,
                nvl(in.getRrule(), ext(in, "rrule")),
                ext(in, "location"),
                uid
        );

        if (in.getBackgroundColor()!=null && !in.getBackgroundColor().isBlank())
            saved.setColor(in.getBackgroundColor());

        Long relatedTaskId = parseLongOrNull(ext(in, "relatedTaskId"));
        if (relatedTaskId != null)
            taskRepository.findById(relatedTaskId).ifPresent(saved::setRelatedTask);

        saved = projectCalendarRepository.save(saved);
        return toEventDto(saved);
    }

    // ───────── 수정 ─────────
    @PutMapping("/events/{id}")
    public EventDto update(@PathVariable Long projectId,
                           @PathVariable("id") String id,
                           @RequestBody EventDto in) {

        if (id.startsWith("T-")) throw new IllegalArgumentException("Task-derived event cannot be edited.");

        ProjectCalendar e = projectCalendarRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));

        // 프로젝트 일치 검증
        if (e.getProject() == null || !Objects.equals(e.getProject().getId(), projectId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Event not in project");
        }

        boolean allDay = in.isAllDay();
        e.setTitle(nvl(in.getTitle(), ""));
        e.setDescription(ext(in, "memo"));
        e.setStartAt(parseFlexible(in.getStart(), false, allDay, e.getStartAt()));
        e.setEndAt(parseFlexible(in.getEnd(),   true,  allDay, e.getEndAt()));
        e.setAllDay(allDay);
        e.setRrule(nvl(in.getRrule(), ext(in, "rrule")));
        e.setLocation(ext(in, "location"));

        if (in.getBackgroundColor()!=null && !in.getBackgroundColor().isBlank())
            e.setColor(in.getBackgroundColor());

        Long relatedTaskId = parseLongOrNull(ext(in, "relatedTaskId"));
        if (relatedTaskId != null) e.setRelatedTask(taskRepository.findById(relatedTaskId).orElse(null));
        else e.setRelatedTask(null);

        e = projectCalendarRepository.save(e);
        return toEventDto(e);
    }

    // ───────── 삭제 ─────────
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId,
                                       @PathVariable("id") String id) {
        if (id.startsWith("T-")) return ResponseEntity.badRequest().build();

        ProjectCalendar e = projectCalendarRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));

        if (e.getProject() == null || !Objects.equals(e.getProject().getId(), projectId)) {
            return ResponseEntity.notFound().build();
        }

        projectCalendarRepository.delete(e);
        return ResponseEntity.ok().build();
    }

    // ───────── 참석자(Attendees) API ─────────

    /** 참석자 조회: 프런트 JS가 { user:{id} } 구조를 기대하므로 거기에 맞춰 반환 */
    @GetMapping("/events/{id}/attendees")
    public List<Map<String, Object>> getAttendees(@PathVariable Long projectId,
                                                  @PathVariable("id") Long eventId) {
        ProjectCalendar ev = projectCalendarRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
        if (ev.getProject() == null || !Objects.equals(ev.getProject().getId(), projectId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Event not in project");
        }

        return eventAttendeeRepository.findByEvent_Id(eventId).stream()
                .map(a -> {
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId());
                    Map<String,Object> u = new HashMap<>();
                    u.put("id", a.getUser().getId());
                    m.put("user", u);
                    return m;
                })
                .toList();
    }

    /** 참석자 저장(전체 교체): body = [1,5,9] 또는 {attendeeIds:[...]} */
    @PutMapping(value = "/events/{id}/attendees", consumes = "application/json")
@Transactional
public ResponseEntity<Void> putAttendees(
        HttpSession session,
        @PathVariable Long projectId,
        @PathVariable("id") Long eventId,
        @RequestBody Object body) {

    Long uid = currentUserId(session);
    if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

    ProjectCalendar event = projectCalendarRepository.findById(eventId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + eventId));

    if (event.getProject() == null || !Objects.equals(event.getProject().getId(), projectId)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event not in project");
    }

    // [1] 바디 파싱(배열 또는 {attendeeIds:[...]})
    List<Long> ids = new ArrayList<>();
    if (body instanceof List<?> arr) {
        for (Object o : arr) if (o instanceof Number n) ids.add(n.longValue());
    } else if (body instanceof Map<?,?>) {
    @SuppressWarnings("unchecked")
    Map<String, Object> map = (Map<String, Object>) body;

    Object cand = map.get("attendeeIds");
    if (cand == null) cand = map.get("userIds");
    if (cand == null) cand = map.get("attendees");

    if (cand instanceof List<?> arr2) {
        for (Object o : arr2) if (o instanceof Number n) ids.add(n.longValue());
    } else {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid body");
    }
    } else {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid body");
    }
    ids = ids.stream().filter(Objects::nonNull).distinct().toList();

    // [2] 전체 교체
    eventAttendeeRepository.deleteByEvent_Id(eventId);

    if (!ids.isEmpty()) {
        List<com.azure.model.calendar.EventAttendee> rows = new ArrayList<>(ids.size());
        for (Long userId : ids) {
            var row = new com.azure.model.calendar.EventAttendee();
            row.setEvent(event);              
            var u = new com.azure.model.user.User(); u.setId(userId);
            row.setUser(u);
            rows.add(row);
        }
        eventAttendeeRepository.saveAll(rows);
    }
    return ResponseEntity.ok().build();
}
}
