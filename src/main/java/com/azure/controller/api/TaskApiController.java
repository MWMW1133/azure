package com.azure.controller.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.azure.dto.TaskUpdateDTO;
import com.azure.model.audit.AuditDiff;
import com.azure.model.enums.AuditEnums.ActionType;
import com.azure.model.enums.PriorityCode;
import com.azure.model.task.Task;
import com.azure.model.user.User;
import com.azure.model.workflow.Workflow;
import com.azure.service.TaskService;
import com.azure.service.AuditService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskApiController {
  private final TaskService taskService;

  @PatchMapping("/{taskId}/assignee")
  public TaskUpdateDTO setAssignee(@PathVariable Long taskId, @RequestBody AssigneeReq req) {
    Task t = taskService.assign(taskId, req.getUserId());

    TaskUpdateDTO dto = new TaskUpdateDTO();
    dto.setId(t.getId());

    User u = t.getAssignee();
    if (u != null) {
      dto.setAssigneeId(u.getId());
    } else {
      dto.setAssigneeId(null);
    }
    return dto;
  }
  @Data
  static class AssigneeReq { Long userId; }

    @PatchMapping("/{taskId}/workflow")
    public TaskUpdateDTO setWorkflow(@PathVariable Long taskId, @RequestBody WorkflowReq req) {
        Task t = taskService.setWorkflow(taskId, req.getWorkflowId());

        TaskUpdateDTO dto = new TaskUpdateDTO();
        dto.setId(t.getId());
        Workflow wf = t.getWorkflow();
        if (wf != null) {
            dto.setWorkflowsId(wf.getId());
        } else {
            dto.setWorkflowsId(null);
        }
        return dto;
    }

    @Data
    static class WorkflowReq { Long workflowId; }

    @PatchMapping("/{taskId}/priority")
    public TaskUpdateDTO setPriority(@PathVariable Long taskId, @RequestBody PriorityReq req) {
        Task t = taskService.setPriority(taskId, req.getPriorityId());
        TaskUpdateDTO dto = new TaskUpdateDTO();
        dto.setId(t.getId());
        dto.setPriorityId(t.getPriority().getId());

        return dto;
    }

    @Data
    static class PriorityReq { Long priorityId; }
}