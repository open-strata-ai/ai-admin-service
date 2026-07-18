package com.openstrata.admin.application;

import com.openstrata.admin.config.TenantContext;
import com.openstrata.admin.domain.DomainException;
import com.openstrata.admin.domain.model.PackageTier;
import com.openstrata.admin.domain.model.TenantGovernance;
import com.openstrata.admin.domain.model.TenantId;
import com.openstrata.admin.domain.port.ControlPlaneClient;
import com.openstrata.admin.infrastructure.persistence.AuditRecorder;
import com.openstrata.admin.infrastructure.persistence.TenantGovernanceMapper;
import com.openstrata.admin.infrastructure.persistence.TenantGovernanceRepository;
import com.openstrata.admin.web.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * ADR-0001 — Governance authority vs. domain authority boundary.
 *
 * <p>Admin-service is the governance orchestrator, NOT the data authority. Every
 * domain write goes through {@link ControlPlaneClient} (the single write point to
 * `ai-platform-api`); this service only persists a local mirror of the governance
 * state (ADR-2) and records audit. Per the "Conservative Default Principle" the
 * existing cross-repo SPI contract is preserved and no destructive changes are
 * made.
 */
@Service
public class GovernanceAuthorityService {

    private final ControlPlaneClient controlPlaneClient;
    private final TenantGovernanceRepository repository;
    private final AuditRecorder auditRecorder;

    public GovernanceAuthorityService(ControlPlaneClient controlPlaneClient,
                                      TenantGovernanceRepository repository,
                                      AuditRecorder auditRecorder) {
        this.controlPlaneClient = controlPlaneClient;
        this.repository = repository;
        this.auditRecorder = auditRecorder;
    }

    public TenantGovernance createTenant(TenantId tenantId, PackageTier tier) {
        TenantGovernance g = new TenantGovernance(tenantId, tier);
        // Single write point: the domain authority owns tenant data.
        controlPlaneClient.createTenant(tenantId, tier);
        // Local mirror only.
        repository.save(TenantGovernanceMapper.toEntity(g));
        auditRecorder.record(actor(), com.openstrata.admin.domain.model.AuditScope.PLATFORM,
            tenantId.value(), "TENANT_CREATED",
            java.util.Map.of("package", tier.name()));
        return g;
    }

    public void suspendTenant(TenantId tenantId) {
        controlPlaneClient.suspendTenant(tenantId);
        auditRecorder.record(actor(), com.openstrata.admin.domain.model.AuditScope.PLATFORM,
            tenantId.value(), "TENANT_SUSPENDED", java.util.Map.of());
    }

    public void resumeTenant(TenantId tenantId) {
        controlPlaneClient.resumeTenant(tenantId);
        auditRecorder.record(actor(), com.openstrata.admin.domain.model.AuditScope.PLATFORM,
            tenantId.value(), "TENANT_RESUMED", java.util.Map.of());
    }

    public TenantGovernance load(TenantId tenantId) {
        return repository.findById(tenantId.value())
            .map(TenantGovernanceMapper::toDomain)
            .orElseThrow(() -> new DomainException(ErrorCode.TENANT_NOT_FOUND,
                "Tenant governance not found: " + tenantId.value()));
    }

    public TenantGovernance save(TenantGovernance g) {
        repository.save(TenantGovernanceMapper.toEntity(g));
        return g;
    }

    public Optional<TenantGovernance> find(TenantId tenantId) {
        return repository.findById(tenantId.value()).map(TenantGovernanceMapper::toDomain);
    }

    public List<String> findAllIds() {
        return repository.findAll().stream()
            .map(com.openstrata.admin.infrastructure.persistence.TenantGovernanceEntity::getTenantId)
            .toList();
    }

    private String actor() {
        String t = TenantContext.tenantId();
        return t == null ? "system" : t;
    }
}
