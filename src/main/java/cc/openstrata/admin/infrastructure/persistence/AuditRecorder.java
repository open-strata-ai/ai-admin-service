package cc.openstrata.admin.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cc.openstrata.admin.domain.model.AuditEntry;
import cc.openstrata.admin.domain.model.AuditScope;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Writes immutable {@link AuditEntry} records to the audit log (RULE-09).
 * The log is INSERT-ONLY — there is no update/delete path. Audit is recorded
 * for every governance change regardless of the security profile.
 */
@Component
public class AuditRecorder {

    private static final ObjectMapper M = new ObjectMapper();

    private final AuditLogRepository repository;

    public AuditRecorder(AuditLogRepository repository) {
        this.repository = repository;
    }

    public AuditEntry record(String actor, AuditScope scope, String tenantId,
                             String action, Map<String, Object> payload) {
        AuditEntryEntity entity = new AuditEntryEntity();
        entity.setActor(actor);
        entity.setScope(scope);
        entity.setTenantId(tenantId);
        entity.setAction(action);
        entity.setCreatedAt(Instant.now());
        if (payload != null && !payload.isEmpty()) {
            try {
                entity.setPayload(M.writeValueAsString(payload));
            } catch (JsonProcessingException e) {
                entity.setPayload("{}");
            }
        }
        AuditEntryEntity saved = repository.save(entity);
        return new AuditEntry(saved.getId(), actor, scope, tenantId, action, payload, saved.getCreatedAt());
    }

    public AuditEntry record(AuditEntry entry) {
        return record(entry.actor(), entry.scope(), entry.tenantId(),
            entry.action(), entry.payload());
    }
}
