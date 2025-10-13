package com.azure.controller.api;

import com.azure.dto.EventDto;
import com.azure.model.calendar.ProjectCalendar;
import com.azure.model.task.Task;
import com.azure.repository.ProjectCalendarRepository;
import com.azure.repository.TaskRepository;
import com.azure.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.azure.repository.EventAttendeeRepository;
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

    /** 목록: 프로젝트 이벤트 + 태스크 파생 이벤트 */
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

    /** 생성 */
    @PostMapping("/events")
    public EventDto create(Long uid,
                           @PathVariable Long projectId,
                           @RequestBody EventDto in) {

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

    /** 수정 */
    @PutMapping("/events/{id}")
    public EventDto update(@PathVariable Long projectId,
                           @PathVariable("id") String id,
                           @RequestBody EventDto in) {

        if (id.startsWith("T-")) throw new IllegalArgumentException("Task-derived event cannot be edited.");

        ProjectCalendar e = projectCalendarRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));

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
        if (relatedTaskId != null) {
            e.setRelatedTask(taskRepository.findById(relatedTaskId).orElse(null));
        } else {
            e.setRelatedTask(null);
        }

        e = projectCalendarRepository.save(e);
        return toEventDto(e);
    }

    /** 삭제 */
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId,
                                       @PathVariable("id") String id) {
        if (id.startsWith("T-")) return ResponseEntity.badRequest().build();
        projectCalendarRepository.deleteById(Long.valueOf(id));
        return ResponseEntity.ok().build();
    }

    // ───────── 매핑/유틸 ─────────
    private EventDto toEventDto(ProjectCalendar e) {
    EventDto dto = new EventDto();
    dto.setId(String.valueOf(e.getId()));
    dto.setTitle(e.getTitle());
    dto.setStart(e.getStartAt().toString());
    dto.setEnd(e.getEndAt().toString());
    dto.setAllDay(Boolean.TRUE.equals(e.getAllDay()));
    dto.setBackgroundColor((e.getColor()==null || e.getColor().isBlank()) ? "blue" : e.getColor());
    dto.setRrule(e.getRrule());

    Map<String,String> ext = new HashMap<>();
    if (e.getLocation()!=null)    ext.put("location", e.getLocation());
    if (e.getDescription()!=null) ext.put("memo", e.getDescription());
    if (e.getRrule()!=null)       ext.put("rrule", e.getRrule());
    if (e.getRelatedTask()!=null) ext.put("relatedTaskId", String.valueOf(e.getRelatedTask().getId()));
    dto.setExtendedProps(ext);

        // ✅ 여기! 인스턴스 주입으로 조회
        List<Long> attendeeIds = eventAttendeeRepository.findByEvent_Id(e.getId())
                .stream()
                .map(a -> a.getUser().getId())
                .toList();
        dto.setAttendeeIds(attendeeIds);

    return dto;
    }

    /** 태스크 → 읽기전용 종일 이벤트 */
    private EventDto toTaskEventDto(Task t) {
        EventDto dto = new EventDto();
        dto.setId("T-" + t.getId());
        dto.setTitle("[태스크] " + nvl(t.getTitle(), ""));
        dto.setStart(t.getStartDate().atStartOfDay().toString());
        dto.setEnd(t.getDueDate().atTime(23,59).toString());
        dto.setAllDay(true);
        dto.setBackgroundColor("gray");
        Map<String,String> ext = new HashMap<>();
        ext.put("relatedTaskId", String.valueOf(t.getId()));
        dto.setExtendedProps(ext);
        return dto;
    }

    private static String ext(EventDto in, String key){ var m = in.getExtendedProps(); return (m==null)?null:m.get(key); }
    private static String nvl(String a, String b){ return (a!=null && !a.isBlank()) ? a : b; }
    private static Long parseLongOrNull(String s){ try { return (s==null || s.isBlank())? null : Long.valueOf(s.trim()); } catch(Exception e){ return null; } }

    /** "yyyy-MM-dd" 또는 "yyyy-MM-ddTHH:mm[:ss]" 허용. allDay면 00:00/23:59 보정 */
    private static LocalDateTime parseFlexible(String s, boolean end, boolean allDay, LocalDateTime fallback) {
        if (s==null || s.isBlank()) return fallback;
        String t = s.trim();
        // "yyyy-MM-dd" 만 들어오면 00:00 또는 23:59로 보정
        if (t.length()==10) {
            var d = LocalDate.parse(t);
            return end ? d.atTime(23,59) : d.atStartOfDay();
        }
        String iso = t.replace(' ', 'T');
        // "yyyy-MM-ddTHH:mm" → "yyyy-MM-ddTHH:mm:00" 보정
        if (iso.length()==16) iso = iso + ":00";
        // "yyyy-MM-ddTHH:mm:ss" → LocalDateTime
        return LocalDateTime.parse(iso.substring(0,19));
    }
}
