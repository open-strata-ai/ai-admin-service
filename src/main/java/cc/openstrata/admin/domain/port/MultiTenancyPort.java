package cc.openstrata.admin.domain.port;

import cc.openstrata.admin.domain.model.IsolationSpec;
import cc.openstrata.admin.domain.model.ResourceQuota;
import cc.openstrata.admin.domain.model.TenantId;

/** MultiTenancy SPI (§8.2). Capsule adapter is the default (advanced/full). */
public interface MultiTenancyPort {
    void deployQuota(TenantId tenantId, IsolationSpec spec);
    ResourceQuota queryUsage(TenantId tenantId);
}
