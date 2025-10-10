package com.azure.controller.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.azure.service.WorkflowService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects/{projectId}/workflows")
@RequiredArgsConstructor
public class WorkflowApiController {

  private final WorkflowService workflowService;

  // 목록 (정렬순)
  @GetMapping
  public List<Map<String,Object>> list(@PathVariable Long projectId) {
    return workflowService.listByProjectOrdered(projectId).stream().map(wf -> {
      Map<String,Object> m = new LinkedHashMap<>();
      m.put("id", wf.getId());
      m.put("name", wf.getName());
      m.put("color", wf.getColor());
      m.put("sortOrder", wf.getSortOrder());
      m.put("is_blocking", wf.getIsBlocking());
      m.put("terminal", wf.getIsTerminal());
      return m;
    }).toList();
  }

  @Data public static class CreateReq {
    private String name; private Integer sortOrder;
    private Boolean blocking; private Boolean terminal; private String color;
  }
  @PostMapping
  public Map<String,Object> create(@PathVariable Long projectId, @RequestBody CreateReq req) {
    var wf = workflowService.create(
        projectId,
        Optional.ofNullable(req.getName()).orElse("New"),
        Optional.ofNullable(req.getSortOrder()).orElse(0),
        Optional.ofNullable(req.getBlocking()).orElse(false),
        Optional.ofNullable(req.getTerminal()).orElse(false),
        Optional.ofNullable(req.getColor()).orElse("#e5e7eb")
    );
    return Map.of("id", wf.getId(), "name", wf.getName(), "color", wf.getColor(),
                  "sortOrder", wf.getSortOrder(), "blocking", wf.getIsBlocking(), "terminal", wf.getIsTerminal());
  }

  @Data public static class UpdateReq {
    private String name; private Integer sortOrder;
    private Boolean blocking; private Boolean terminal; private String color;
  }
  @PatchMapping("/{workflowId}")
  public Map<String,Object> update(@PathVariable Long workflowId, @RequestBody UpdateReq req) {
    var wf = workflowService.update(workflowId, req.getName(), req.getSortOrder(),
                                    req.getBlocking(), req.getTerminal(), req.getColor());
    return Map.of("id", wf.getId(), "name", wf.getName(), "color", wf.getColor(),
                  "sortOrder", wf.getSortOrder(), "blocking", wf.getIsBlocking(), "terminal", wf.getIsTerminal());
  }

  @DeleteMapping("/{workflowId}")
  public void delete(@PathVariable Long workflowId) {
    workflowService.delete(workflowId);
  }
}