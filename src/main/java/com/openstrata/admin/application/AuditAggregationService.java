package com.openstrata.admin.application;

import com.openstrata.admin.domain.model.AuditEntry;
import com.openstrata.admin.domain.model.AuditScope;
import com.openstrata.admin.infrastructure.persistence.AuditLogRepository;
import com.openstrata.admin.infrastructure.persistence.AuditRecorder;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * ADR-0004 — Audit cross-service aggregation.
 *
 * <p>This service owns the management-plane audit (local `audit_log`). Business-
 * plane audit lives in platform-api / other services. This service records
 * immutable entries (RULE-09) and aggregates a combined view across local +
 * (eventually) external sources. Conservative default: local-only aggregation
 * until the unified audit bus (§4.7.4) is adopted.
 */
@Service
public class AuditAggregationService {

    public record AggregatedAudit(long localCount, long externalCount,
                                  List<AuditEntry> local, String status) {}

    private final AuditLogRepository auditLogRepository;
    private final AuditRecorder auditRecorder;

    public AuditAggregationService(AuditLogRepository auditLogRepository,
                                   AuditRecorder auditRecorder) {
        this.auditLogRepository = auditLogRepository;
        this.auditRecorder = auditRecorder;
    }

    public AuditEntry record(String actor, AuditScope scope, String tenantId,
                             String action, Map<String, Object> payload) {
        return auditRecorder.record(actor, scope, tenantId, action, payload);
    }

    public List<AuditEntry> query(AuditScope scope, String tenantId) {
        if (scope != null && tenantId != null) {
            return auditLogRepository.findByTenantId(tenantId).stream()
                .filter(e -> e.getScope() == scope)
                .map(this::toDomain).toList();
        }
        if (scope != null) {
            return auditLogRepository.findByScope(scope).stream().map(this::toDomain).toList();
        }
        if (tenantId != null) {
            return auditLogRepository.findByTenantId(tenantId).stream().map(this::toDomain).toList();
        }
        return auditLogRepository.findAll().stream().map(this::toDomain).toList();
    }

    public AggregatedAudit aggregateCrossService(String tenantId) {
        List<AuditEntry> local = auditLogRepository.findByTenantId(tenantId).stream()
            .map(this::toDomain).toList();
        // External (business-plane) audit not yet federated; placeholder count 0.
        return new AggregatedAudit(local.size(), 0, local, "LOCAL_ONLY");
    }

    private AuditEntry toDomain(com.openstrata.admin.infrastructure.persistence.AuditEntryEntity e) {
        return new AuditEntry(e.getId(), e.getActor(), e.getScope(), e.getTenantId(),
            e.getAction(), Map.of(), e.getCreatedAt());
    }
}
