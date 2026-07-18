package com.openstrata.admin.domain.model;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable audit entry (RULE-09 / §14.6). Once written it is never updated or
 * deleted — the audit log is INSERT-ONLY even when the security profile is off.
 */
public record AuditEntry(Long id, String actor, AuditScope scope, String tenantId,
                         String action, Map<String, Object> payload, Instant at) {

    public static AuditEntry of(String actor, AuditScope scope, String tenantId,
                                String action, Map<String, Object> payload) {
        return new AuditEntry(null, actor, scope, tenantId, action, payload, Instant.now());
    }
}
