package com.azure.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.azure.dto.AuditLogDTO;
import com.azure.model.audit.AuditDiff;
import com.azure.model.audit.AuditLog;
import com.azure.model.enums.AuditEnums;
import com.azure.model.enums.AuditEnums.ActionType;
import com.azure.model.enums.AuditEnums.EntityType;
import com.azure.model.user.User;
import com.azure.repository.AuditLogRepository;
import com.azure.service.AuditService;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {
    private final AuditLogRepository repo;
    private final ObjectMapper objectMapper; 

    @Override
    @Transactional
    public void log(User actor, EntityType entityType, Long entityId, ActionType action, AuditDiff diff) {
        AuditLog row = new AuditLog();
        row.setActor(actor);
        row.setEntityType(entityType.name());
        row.setEntityId(entityId);
        row.setAction(action.name());
        row.setDiffJson(diff == null ? "{}" : diff.toJson(objectMapper));
        repo.save(row);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> listTaskAudits(Long taskId) {
        List<AuditLog> logs = repo
                .findByEntityTypeAndEntityIdOrderByIdDesc(
                        AuditEnums.EntityType.TASK.name(), taskId);

        return logs.stream().map(l -> {
            AuditLogDTO dto = new AuditLogDTO();
            dto.setId(l.getId());
            dto.setCreatedAt(l.getCreatedAt());
            dto.setAction(l.getAction());
            // actor가 null일 수 있으므로 NPE 방지
            dto.setActorId(l.getActor() != null ? l.getActor().getId() : null);
            dto.setEntityType(l.getEntityType());
            dto.setEntityId(l.getEntityId());
            // diffJson은 문자열 그대로 (프런트에서 parse)
            dto.setDiffJson(l.getDiffJson() != null ? l.getDiffJson() : "{}");
            return dto;
        }).toList();
    }

}
