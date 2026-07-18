package com.openstrata.admin.domain.port;

import com.openstrata.admin.domain.model.EntitlementSet;
import com.openstrata.admin.domain.model.PackageTier;
import com.openstrata.admin.domain.model.ResourceQuota;
import com.openstrata.admin.domain.model.TenantId;

/**
 * Control-plane SPI to `ai-platform-api` — the single write path to domain
 * authority data (RULE-07 / ADR-0001). Admin-service NEVER writes platform-api's
 * database directly; it orchestrates through this port (anti-corruption layer).
 */
public interface ControlPlaneClient {

    void createTenant(TenantId tenantId, PackageTier tier);
    void updatePackage(TenantId tenantId, PackageTier tier);
    void updateQuota(TenantId tenantId, ResourceQuota quota);
    void setEntitlements(TenantId tenantId, EntitlementSet entitlements);
    void suspendTenant(TenantId tenantId);
    void resumeTenant(TenantId tenantId);

    /** Read projection: allocated quota for a tenant (DESIGN §9). */
    PlatformProfile getProfile(TenantId tenantId);

    record PlatformProfile(ResourceQuota allocated) {}
}
