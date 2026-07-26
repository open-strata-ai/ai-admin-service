package cc.openstrata.admin.application;

import cc.openstrata.admin.application.dto.ProviderView;
import cc.openstrata.admin.application.dto.UpsertProviderRequest;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.domain.model.AuditScope;
import cc.openstrata.admin.domain.port.SecretStorePort;
import cc.openstrata.admin.infrastructure.persistence.ProviderEntity;
import cc.openstrata.admin.infrastructure.persistence.ProviderJpaRepository;
import cc.openstrata.admin.web.ErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Use case: LLM provider registry (PR-C). Credentials are delegated to the
 *  configured secret store; only an opaque reference is kept on the row. */
@Service
public class ProviderAppService {

    private final ProviderJpaRepository repo;
    private final SecretStorePort secrets;
    private final AuditAggregationService audit;

    public ProviderAppService(ProviderJpaRepository repo, SecretStorePort secrets,
                              AuditAggregationService audit) {
        this.repo = repo;
        this.secrets = secrets;
        this.audit = audit;
    }

    public ProviderView create(UpsertProviderRequest req) {
        ProviderEntity e = new ProviderEntity();
        e.setId("prov-" + UUID.randomUUID().toString().substring(0, 8));
        map(e, req);
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        repo.save(e);
        audit.record(Actors.current(), AuditScope.PLATFORM, null, "PROVIDER_CREATED",
            Map.of("id", e.getId(), "type", e.getType()));
        return toView(e);
    }

    public ProviderView update(String id, UpsertProviderRequest req) {
        ProviderEntity e = require(id);
        map(e, req);
        e.setUpdatedAt(Instant.now());
        repo.save(e);
        audit.record(Actors.current(), AuditScope.PLATFORM, null, "PROVIDER_UPDATED",
            Map.of("id", id));
        return toView(e);
    }

    public List<ProviderView> list() {
        return repo.findAll().stream().map(this::toView).toList();
    }

    public ProviderView get(String id) {
        return toView(require(id));
    }

    public void delete(String id) {
        ProviderEntity e = require(id);
        if (e.getSecretRef() != null) {
            try {
                secrets.delete(e.getSecretRef());
            } catch (RuntimeException ex) {
                // best-effort cleanup of the secret reference
            }
        }
        repo.deleteById(id);
        audit.record(Actors.current(), AuditScope.PLATFORM, null, "PROVIDER_DELETED",
            Map.of("id", id));
    }

    /** Store the provider credential in the secret store and keep only the ref. */
    public void setSecret(String id, String secret) {
        ProviderEntity e = require(id);
        String ref = secrets.store(id, secret);
        e.setSecretRef(ref);
        e.setUpdatedAt(Instant.now());
        repo.save(e);
        audit.record(Actors.current(), AuditScope.PLATFORM, null, "PROVIDER_SECRET_SET",
            Map.of("id", id));
    }

    public void deleteSecret(String id) {
        ProviderEntity e = require(id);
        if (e.getSecretRef() != null) {
            secrets.delete(e.getSecretRef());
            e.setSecretRef(null);
            e.setUpdatedAt(Instant.now());
            repo.save(e);
            audit.record(Actors.current(), AuditScope.PLATFORM, null, "PROVIDER_SECRET_DELETED",
                Map.of("id", id));
        }
    }

    private void map(ProviderEntity e, UpsertProviderRequest req) {
        e.setName(req.name());
        e.setType(req.type());
        e.setBaseUrl(req.baseUrl());
        e.setAuthType(req.authType() == null ? "api_key" : req.authType());
        e.setStatus(req.status() == null ? "ACTIVE" : req.status());
        e.setLabels(req.labels() == null ? null : JsonSupport.writeMap(req.labels()));
    }

    private ProviderEntity require(String id) {
        return repo.findById(id).orElseThrow(() -> new DomainException(
            ErrorCode.PROVIDER_NOT_FOUND, "provider not found: " + id));
    }

    private ProviderView toView(ProviderEntity e) {
        return new ProviderView(e.getId(), e.getName(), e.getType(), e.getBaseUrl(),
            e.getAuthType(), e.getStatus(), e.getSecretRef() != null,
            JsonSupport.readMap(e.getLabels()), e.getCreatedAt(), e.getUpdatedAt());
    }
}
