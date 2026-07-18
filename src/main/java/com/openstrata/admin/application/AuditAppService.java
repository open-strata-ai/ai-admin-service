package com.openstrata.admin.application;

import com.openstrata.admin.domain.model.AuditEntry;
import com.openstrata.admin.domain.model.AuditScope;
import java.util.List;
import org.springframework.stereotype.Service;

/** Use case: audit query / cross-service aggregation (ADR-0004). DESIGN §4. */
@Service
public class AuditAppService {

    private final AuditAggregationService auditAggregation;

    public AuditAppService(AuditAggregationService auditAggregation) {
        this.auditAggregation = auditAggregation;
    }

    public List<AuditEntry> query(AuditScope scope, String tenantId) {
        return auditAggregation.query(scope, tenantId);
    }

    public AuditAggregationService.AggregatedAudit aggregate(String tenantId) {
        return auditAggregation.aggregateCrossService(tenantId);
    }
}
