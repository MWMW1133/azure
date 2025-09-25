package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.audit.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> { }
