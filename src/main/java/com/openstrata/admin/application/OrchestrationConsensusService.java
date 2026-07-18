package com.openstrata.admin.application;

import com.openstrata.admin.domain.DomainException;
import com.openstrata.admin.domain.model.ProvisioningPlan;
import com.openstrata.admin.domain.model.ProvisioningStatus;
import com.openstrata.admin.domain.model.TenantId;
import com.openstrata.admin.infrastructure.persistence.ProvisioningPlanEntity;
import com.openstrata.admin.infrastructure.persistence.ProvisioningPlanRepository;
import com.openstrata.admin.web.ErrorCode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * ADR-0003 — Orchestration eventually-consistent SLA.
 *
 * <p>Tracks `provisioning_plans` through PENDING → APPLYING → DONE/FAILED. A
 * tenant may have at most one active plan (no concurrent drift). Plans stuck in
 * APPLYING beyond the SLA are failed (retryable) — the conservative default
 * until the dependent capability (§13.5 rollback) is solidified.
 */
@Service
public class OrchestrationConsensusService {

    /** Default convergence SLA for a provisioning plan. */
    public static final Duration SLA = Duration.ofMinutes(30);

    private final ProvisioningPlanRepository repository;

    public OrchestrationConsensusService(ProvisioningPlanRepository repository) {
        this.repository = repository;
    }

    public ProvisioningPlan start(TenantId tenantId, String manifest) {
        if (!repository.findActiveByTenant(tenantId.value()).isEmpty()) {
            throw new DomainException(ErrorCode.PROVISIONING_IN_PROGRESS,
                "Another provisioning plan is active for tenant " + tenantId.value());
        }
        ProvisioningPlanEntity e = new ProvisioningPlanEntity();
        e.setPlanId("plan-" + UUID.randomUUID().toString().substring(0, 8));
        e.setTenantId(tenantId.value());
        e.setManifest(manifest);
        e.setStatus(ProvisioningStatus.PENDING);
        e.setCreatedAt(Instant.now());
        e = repository.save(e);
        return toDomain(e);
    }

    public ProvisioningPlan transition(String planId, ProvisioningStatus next) {
        ProvisioningPlanEntity e = repository.findById(planId)
            .orElseThrow(() -> new DomainException(ErrorCode.BAD_REQUEST,
                "Provisioning plan not found: " + planId));
        if (!canTransition(e.getStatus(), next)) {
            throw new DomainException(ErrorCode.BAD_REQUEST,
                "Illegal transition " + e.getStatus() + " -> " + next);
        }
        e.setStatus(next);
        return toDomain(repository.save(e));
    }

    /** Consensus = plan reached DONE (manifest applied + provisioning confirmed). */
    public boolean isConsensusReached(String planId) {
        return repository.findById(planId)
            .map(e -> e.getStatus() == ProvisioningStatus.DONE)
            .orElse(false);
    }

    /** Fail plans stuck in APPLYING beyond the SLA (retryable). */
    public int failTimedOut(Instant now) {
        List<ProvisioningPlanEntity> stuck = repository.findAll().stream()
            .filter(e -> e.getStatus() == ProvisioningStatus.APPLYING)
            .filter(e -> Duration.between(e.getCreatedAt(), now).compareTo(SLA) > 0)
            .toList();
        stuck.forEach(e -> e.setStatus(ProvisioningStatus.FAILED));
        repository.saveAll(stuck);
        return stuck.size();
    }

    public static boolean canTransition(ProvisioningStatus from, ProvisioningStatus to) {
        return switch (from) {
            case PENDING -> to == ProvisioningStatus.APPLYING || to == ProvisioningStatus.FAILED;
            case APPLYING -> to == ProvisioningStatus.DONE || to == ProvisioningStatus.FAILED;
            case DONE, FAILED -> false; // terminal
        };
    }

    private ProvisioningPlan toDomain(ProvisioningPlanEntity e) {
        return new ProvisioningPlan(e.getPlanId(), new TenantId(e.getTenantId()),
            e.getManifest(), e.getStatus(), e.getCreatedAt());
    }
}
