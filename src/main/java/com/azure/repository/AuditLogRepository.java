package com.azure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.audit.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByIdDesc(String entityType, Long entityId);
}
