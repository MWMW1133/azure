package com.azure.controller;

import com.azure.dto.TaskCreateDTO;
import com.azure.dto.TaskResponseDTO;
import com.azure.model.task.Task;
import com.azure.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskController {

    private final TaskService taskService;

    /** ✅ 목록 조회 */
    @GetMapping
    public List<Map<String, Object>> list(@PathVariable Long projectId) {
        return taskService.listByProject(projectId).stream()
                .map(t -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", t.getId());
                    map.put("title", t.getTitle());
                    map.put("assignee",
                            (t.getAssignee() != null && t.getAssignee().getName() != null)
                                    ? t.getAssignee().getName() : "-");
                    map.put("startDate", t.getStartDate());
                    map.put("dueDate", t.getDueDate());
                    map.put("status",
                            (t.getWorkflow() != null && t.getWorkflow().getName() != null)
                                    ? t.getWorkflow().getName() : "-");
                    map.put("priority",
                            (t.getPriority() != null && t.getPriority().getName() != null)
                                    ? t.getPriority().getName() : "-");
                    map.put("childrenCount", t.getChildrenCount());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{parentId}/children")
    public List<Map<String, Object>> getChildren(@PathVariable Long projectId, @PathVariable Long parentId) {
        // 목록 조회 메소드의 매핑 로직을 재사용할 수 있습니다.
        return taskService.getSubTasks(parentId).stream()
                .map(t -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", t.getId());
                    map.put("title", t.getTitle());
                    map.put("assignee", (t.getAssignee() != null) ? t.getAssignee().getName() : "-");
                    map.put("startDate", t.getStartDate());
                    map.put("dueDate", t.getDueDate());
                    map.put("status", (t.getWorkflow() != null) ? t.getWorkflow().getName() : "-");
                    map.put("priority", (t.getPriority() != null) ? t.getPriority().getName() : "-");
                    map.put("childrenCount", t.getChildrenCount()); // 중첩된 하위 태스크를 위해 추가
                    return map;
                })
                .collect(Collectors.toList());
    }

    /** ✅ 생성 */
    @PostMapping
@Transactional
public ResponseEntity<TaskResponseDTO> create(
        @PathVariable Long projectId,
        @RequestBody TaskCreateDTO dto) {

    Task saved = taskService.createTask(
            projectId,
            dto.getAssigneeId(),
            dto.getTitle(),
            dto.getWorkflowsId(),
            dto.getPriorityId(),
            dto.getStartDate(),    
            dto.getDueDate(),      
            dto.getParentTaskId()   
    );

    TaskResponseDTO res = new TaskResponseDTO();
    res.setId(saved.getId());
    res.setTitle(saved.getTitle());
    res.setStartDate(saved.getStartDate());
    res.setDueDate(saved.getDueDate());
    res.setAssigneeName(
        saved.getAssignee() != null ? saved.getAssignee().getName() : null
    );
    res.setWorkflowName(
        saved.getWorkflow() != null ? saved.getWorkflow().getName() : null
    );
    res.setPriorityName(
        saved.getPriority() != null ? saved.getPriority().getName() : null
    );

    return ResponseEntity.ok(res);
}

    /** ✅ 여러 태스크 삭제 */
    @DeleteMapping("/bulk-delete")
    @Transactional
    public void deleteTasks(@PathVariable Long projectId, @RequestBody List<Long> ids) {
        taskService.deleteTasks(ids);
    }

    
}
