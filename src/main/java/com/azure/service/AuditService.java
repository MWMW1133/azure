package com.azure.service;


import java.util.List;

import com.azure.dto.AuditLogDTO;
import com.azure.model.audit.AuditDiff;
import com.azure.model.enums.AuditEnums.ActionType;
import com.azure.model.enums.AuditEnums.EntityType;
import com.azure.model.user.User;




public interface AuditService {

  void log(User actor, EntityType entityType, Long entityId, ActionType action, AuditDiff diff);

  List<AuditLogDTO> listTaskAudits(Long taskId);
}