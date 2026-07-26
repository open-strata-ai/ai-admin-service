package cc.openstrata.admin.application;

import cc.openstrata.admin.application.dto.ApiKeyGenerated;
import cc.openstrata.admin.application.dto.ApiKeyView;
import cc.openstrata.admin.application.dto.CreateApiKeyRequest;
import cc.openstrata.admin.config.TenantContext;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.domain.model.AuditScope;
import cc.openstrata.admin.infrastructure.persistence.ApiKeyEntity;
import cc.openstrata.admin.infrastructure.persistence.ApiKeyJpaRepository;
import cc.openstrata.admin.web.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Use case: API key lifecycle (PR-C). The plaintext is returned once at
 *  creation; only a SHA-256 hash + identifying prefix are persisted. */
@Service
public class ApiKeyAppService {

    private static final SecureRandom RNG = new SecureRandom();

    private final ApiKeyJpaRepository repo;
    private final AuditAggregationService audit;

    public ApiKeyAppService(ApiKeyJpaRepository repo, AuditAggregationService audit) {
        this.repo = repo;
        this.audit = audit;
    }

    public ApiKeyGenerated create(CreateApiKeyRequest req) {
        enforceWriteTenant(req.tenantId());
        String plain = generatePlaintext();
        ApiKeyEntity e = new ApiKeyEntity();
        e.setId("key-" + UUID.randomUUID().toString().substring(0, 8));
        e.setName(req.name());
        e.setTenantId(req.tenantId());
        e.setOwnerEmail(req.ownerEmail());
        e.setPrefix(plain.substring(0, Math.min(8, plain.length())));
        e.setKeyHash(hash(plain));
        e.setScopes(req.scopes() == null ? null : JsonSupport.write(req.scopes()));
        e.setStatus("ACTIVE");
        e.setExpiresAt(req.expiresAt());
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        repo.save(e);
        audit.record(Actors.current(), AuditScope.PLATFORM, req.tenantId(), "API_KEY_CREATED",
            Map.of("id", e.getId(), "prefix", e.getPrefix()));
        return new ApiKeyGenerated(toView(e), plain);
    }

    public List<ApiKeyView> list() {
        if (TenantContext.isPlatformAdmin()) {
            return repo.findAll().stream().map(this::toView).toList();
        }
        return repo.findByTenantId(TenantContext.tenantId()).stream().map(this::toView).toList();
    }

    public List<ApiKeyView> listByTenant(String tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toView).toList();
    }

    public ApiKeyView get(String id) {
        return toView(requireOwned(id));
    }

    public void revoke(String id) {
        ApiKeyEntity e = requireOwned(id);
        e.setStatus("REVOKED");
        e.setUpdatedAt(Instant.now());
        repo.save(e);
        audit.record(Actors.current(), AuditScope.PLATFORM, e.getTenantId(), "API_KEY_REVOKED",
            Map.of("id", id));
    }

    private String generatePlaintext() {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        return "sk-" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private ApiKeyEntity require(String id) {
        return repo.findById(id).orElseThrow(() -> new DomainException(
            ErrorCode.API_KEY_NOT_FOUND, "api key not found: " + id));
    }

    private ApiKeyEntity requireOwned(String id) {
        ApiKeyEntity e = require(id);
        enforceTenantScope(e.getTenantId());
        return e;
    }

    /** Enforces that a non-platform-admin actor may only read/write resources
     *  that belong to their own tenant. Platform-admins are exempt. */
    private void enforceTenantScope(String resourceTenant) {
        if (TenantContext.isPlatformAdmin()) {
            return;
        }
        String current = TenantContext.tenantId();
        if (current == null || !current.equals(resourceTenant)) {
            throw new DomainException(ErrorCode.FORBIDDEN,
                "resource belongs to a different tenant");
        }
    }

    /** Enforces that a non-platform-admin actor may only create/revoke resources
     *  for their own tenant (prevents cross-tenant write escalation). */
    private void enforceWriteTenant(String requestedTenant) {
        if (TenantContext.isPlatformAdmin()) {
            return;
        }
        String current = TenantContext.tenantId();
        if (current == null || !current.equals(requestedTenant)) {
            throw new DomainException(ErrorCode.FORBIDDEN,
                "cannot write resources for a different tenant");
        }
    }

    private ApiKeyView toView(ApiKeyEntity e) {
        return new ApiKeyView(e.getId(), e.getName(), e.getTenantId(), e.getOwnerEmail(),
            e.getPrefix(), JsonSupport.read(e.getScopes()), e.getStatus(),
            e.getExpiresAt(), e.getLastUsedAt(), e.getCreatedAt());
    }
}
