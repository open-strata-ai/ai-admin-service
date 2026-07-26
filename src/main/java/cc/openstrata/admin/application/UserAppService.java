package cc.openstrata.admin.application;

import cc.openstrata.admin.application.dto.UpsertUserRequest;
import cc.openstrata.admin.application.dto.UserView;
import cc.openstrata.admin.config.TenantContext;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.domain.model.AuditScope;
import cc.openstrata.admin.infrastructure.persistence.PlatformUserEntity;
import cc.openstrata.admin.infrastructure.persistence.PlatformUserJpaRepository;
import cc.openstrata.admin.web.ErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** Use case: platform user directory (PR-C). Roles include `viewer` (M1). Actual
 *  authentication is delegated to the IdP; this is the governance record. */
@Service
public class UserAppService {

    /** Allowed roles (M1 roadmap). {@code viewer} is read-only. */
    public static final Set<String> ALLOWED_ROLES = Set.of(
        "platform-admin", "tenant-admin", "member", "viewer");

    private final PlatformUserJpaRepository repo;
    private final AuditAggregationService audit;

    public UserAppService(PlatformUserJpaRepository repo, AuditAggregationService audit) {
        this.repo = repo;
        this.audit = audit;
    }

    public UserView create(UpsertUserRequest req) {
        enforceWriteTenant(req.tenantId());
        List<String> roles = normalizeRoles(req.roles());
        PlatformUserEntity e = new PlatformUserEntity();
        e.setId("usr-" + UUID.randomUUID().toString().substring(0, 8));
        map(e, req, roles);
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        repo.save(e);
        audit.record(Actors.current(), AuditScope.PLATFORM, req.tenantId(), "USER_CREATED",
            Map.of("id", e.getId(), "email", e.getEmail()));
        return toView(e);
    }

    public UserView update(String id, UpsertUserRequest req) {
        PlatformUserEntity e = requireOwned(id);
        enforceWriteTenant(req.tenantId());
        List<String> roles = normalizeRoles(req.roles());
        map(e, req, roles);
        e.setUpdatedAt(Instant.now());
        repo.save(e);
        audit.record(Actors.current(), AuditScope.PLATFORM, e.getTenantId(), "USER_UPDATED",
            Map.of("id", id));
        return toView(e);
    }

    public List<UserView> list() {
        if (TenantContext.isPlatformAdmin()) {
            return repo.findAll().stream().map(this::toView).toList();
        }
        return repo.findByTenantId(TenantContext.tenantId()).stream().map(this::toView).toList();
    }

    public List<UserView> listByTenant(String tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toView).toList();
    }

    public UserView get(String id) {
        return toView(requireOwned(id));
    }

    public void delete(String id) {
        PlatformUserEntity e = requireOwned(id);
        repo.deleteById(id);
        audit.record(Actors.current(), AuditScope.PLATFORM, e.getTenantId(), "USER_DELETED",
            Map.of("id", id));
    }

    private List<String> normalizeRoles(List<String> roles) {
        List<String> normalized = (roles == null ? List.<String>of() : roles).stream()
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .collect(Collectors.toList());
        for (String r : normalized) {
            if (!ALLOWED_ROLES.contains(r)) {
                throw new DomainException(ErrorCode.INVALID_ROLE, "unknown role: " + r);
            }
        }
        return normalized;
    }

    private void map(PlatformUserEntity e, UpsertUserRequest req, List<String> roles) {
        e.setEmail(req.email());
        e.setName(req.name());
        e.setTenantId(req.tenantId());
        e.setRoles(JsonSupport.write(roles));
        e.setStatus(req.status() == null ? "ACTIVE" : req.status());
    }

    private PlatformUserEntity require(String id) {
        return repo.findById(id).orElseThrow(() -> new DomainException(
            ErrorCode.USER_NOT_FOUND, "user not found: " + id));
    }

    private PlatformUserEntity requireOwned(String id) {
        PlatformUserEntity e = require(id);
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

    /** Enforces that a non-platform-admin actor may only create/update resources
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

    private UserView toView(PlatformUserEntity e) {
        return new UserView(e.getId(), e.getEmail(), e.getName(), e.getTenantId(),
            JsonSupport.read(e.getRoles()), e.getStatus(),
            e.getCreatedAt(), e.getUpdatedAt());
    }
}
