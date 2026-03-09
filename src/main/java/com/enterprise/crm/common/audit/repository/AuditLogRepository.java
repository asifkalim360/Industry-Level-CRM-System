package com.enterprise.crm.common.audit.repository;

import com.enterprise.crm.common.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
