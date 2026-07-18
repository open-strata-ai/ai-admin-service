package com.openstrata.admin.infrastructure.persistence;

import com.openstrata.admin.domain.model.AuditScope;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditEntryEntity, Long> {

    List<AuditEntryEntity> findByTenantId(String tenantId);
    List<AuditEntryEntity> findByScope(AuditScope scope);
}
