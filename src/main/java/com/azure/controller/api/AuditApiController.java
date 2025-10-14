package com.azure.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.azure.dto.AuditLogDTO;
import com.azure.service.AuditService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class AuditApiController {
    private final AuditService auditService;

    @GetMapping("/{taskId}/audits")
    public List<AuditLogDTO> listAudits(@PathVariable Long taskId) {
        return auditService.listTaskAudits(taskId);
    }
}
