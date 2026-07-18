package com.openstrata.admin.infrastructure.adapter;

import com.openstrata.admin.domain.model.EntitlementSet;
import com.openstrata.admin.domain.model.PackageTier;
import com.openstrata.admin.domain.model.ResourceQuota;
import com.openstrata.admin.domain.model.TenantId;
import com.openstrata.admin.domain.port.ControlPlaneClient;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * In-memory ControlPlaneClient standing in for `ai-platform-api` (the domain
 * authority). The ACL translates governance DTOs to the platform domain model.
 * Production swaps this for the REST adapter; the port contract is unchanged.
 */
@Component
public class InMemoryControlPlaneClient implements ControlPlaneClient {

    private final Map<String, PackageTier> tenants = new ConcurrentHashMap<>();

    @Override
    public void createTenant(TenantId tenantId, PackageTier tier) {
        tenants.put(tenantId.value(), tier);
    }

    @Override
    public void updatePackage(TenantId tenantId, PackageTier tier) {
        tenants.put(tenantId.value(), tier);
    }

    @Override
    public void updateQuota(TenantId tenantId, ResourceQuota quota) {
        // domain authority write (mirrored locally by GovernanceAuthorityService)
    }

    @Override
    public void setEntitlements(TenantId tenantId, EntitlementSet entitlements) {
        // domain authority write
    }

    @Override
    public void suspendTenant(TenantId tenantId) {
        // state transition recorded in platform-api
    }

    @Override
    public void resumeTenant(TenantId tenantId) {
        // state transition recorded in platform-api
    }

    @Override
    public PlatformProfile getProfile(TenantId tenantId) {
        return new PlatformProfile(new ResourceQuota(4, 8, 0, 1_000_000, 200, 4));
    }

    public Set<String> listTenants() {
        return tenants.keySet();
    }
}
