package cc.openstrata.admin.application;

import cc.openstrata.admin.application.dto.ModelView;
import cc.openstrata.admin.application.dto.UpsertModelRequest;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.domain.model.AuditScope;
import cc.openstrata.admin.infrastructure.persistence.ModelEntity;
import cc.openstrata.admin.infrastructure.persistence.ModelJpaRepository;
import cc.openstrata.admin.infrastructure.persistence.ProviderJpaRepository;
import cc.openstrata.admin.web.ErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Use case: model catalog (PR-C). Models are bound to a provider. */
@Service
public class ModelAppService {

    private final ModelJpaRepository repo;
    private final ProviderJpaRepository providers;
    private final AuditAggregationService audit;

    public ModelAppService(ModelJpaRepository repo, ProviderJpaRepository providers,
                           AuditAggregationService audit) {
        this.repo = repo;
        this.providers = providers;
        this.audit = audit;
    }

    public ModelView create(UpsertModelRequest req) {
        requireProvider(req.providerId());
        ModelEntity e = new ModelEntity();
        e.setId("model-" + UUID.randomUUID().toString().substring(0, 8));
        map(e, req);
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        repo.save(e);
        audit.record(Actors.current(), AuditScope.PLATFORM, null, "MODEL_CREATED",
            Map.of("id", e.getId(), "providerId", e.getProviderId()));
        return toView(e);
    }

    public ModelView update(String id, UpsertModelRequest req) {
        ModelEntity e = require(id);
        if (req.providerId() != null && !req.providerId().equals(e.getProviderId())) {
            requireProvider(req.providerId());
            e.setProviderId(req.providerId());
        }
        map(e, req);
        e.setUpdatedAt(Instant.now());
        repo.save(e);
        audit.record(Actors.current(), AuditScope.PLATFORM, null, "MODEL_UPDATED",
            Map.of("id", id));
        return toView(e);
    }

    public List<ModelView> list() {
        return repo.findAll().stream().map(this::toView).toList();
    }

    public List<ModelView> listByProvider(String providerId) {
        return repo.findByProviderId(providerId).stream().map(this::toView).toList();
    }

    public ModelView get(String id) {
        return toView(require(id));
    }

    public void delete(String id) {
        require(id);
        repo.deleteById(id);
        audit.record(Actors.current(), AuditScope.PLATFORM, null, "MODEL_DELETED",
            Map.of("id", id));
    }

    private void requireProvider(String providerId) {
        if (providers.findById(providerId).isEmpty()) {
            throw new DomainException(ErrorCode.PROVIDER_NOT_FOUND,
                "provider not found: " + providerId);
        }
    }

    private void map(ModelEntity e, UpsertModelRequest req) {
        if (req.providerId() != null) e.setProviderId(req.providerId());
        e.setName(req.name());
        e.setFamily(req.family());
        e.setContextWindow(req.contextWindow());
        e.setMaxOutputTokens(req.maxOutputTokens());
        e.setCapabilities(req.capabilities() == null ? null : JsonSupport.write(req.capabilities()));
        e.setStatus(req.status() == null ? "ACTIVE" : req.status());
    }

    private ModelEntity require(String id) {
        return repo.findById(id).orElseThrow(() -> new DomainException(
            ErrorCode.MODEL_NOT_FOUND, "model not found: " + id));
    }

    private ModelView toView(ModelEntity e) {
        return new ModelView(e.getId(), e.getProviderId(), e.getName(), e.getFamily(),
            e.getContextWindow(), e.getMaxOutputTokens(),
            JsonSupport.read(e.getCapabilities()), e.getStatus(),
            e.getCreatedAt(), e.getUpdatedAt());
    }
}
