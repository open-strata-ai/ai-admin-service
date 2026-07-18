package com.openstrata.admin.domain.port;

import com.openstrata.admin.domain.model.TenantId;

/** Cost SPI (§8.3 / §14.4). ai-billing-service + OpenCost (advanced/full). */
public interface CostPort {
    double getTenantCost(TenantId tenantId);
}
