package com.openstrata.admin.domain.port;

import com.openstrata.admin.domain.model.IsolationSpec;
import com.openstrata.admin.domain.model.ResourceQuota;
import com.openstrata.admin.domain.model.TenantId;

/** MultiTenancy SPI (§8.2). Capsule adapter is the default (advanced/full). */
public interface MultiTenancyPort {
    void deployQuota(TenantId tenantId, IsolationSpec spec);
    ResourceQuota queryUsage(TenantId tenantId);
}
