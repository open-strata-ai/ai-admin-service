package cc.openstrata.admin.infrastructure.adapter;

import cc.openstrata.admin.domain.model.IsolationSpec;
import cc.openstrata.admin.domain.model.ResourceQuota;
import cc.openstrata.admin.domain.model.TenantId;
import cc.openstrata.admin.domain.port.MultiTenancyPort;
import org.springframework.stereotype.Component;

/** In-memory MultiTenancyPort standing in for Capsule. */
@Component
public class InMemoryMultiTenancyAdapter implements MultiTenancyPort {

    @Override
    public void deployQuota(TenantId tenantId, IsolationSpec spec) {
        // Capsule Tenant CRD / ResourceQuota applied here
    }

    @Override
    public ResourceQuota queryUsage(TenantId tenantId) {
        return new ResourceQuota(2, 4, 0, 500_000, 100, 2);
    }
}
