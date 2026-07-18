package com.openstrata.admin.domain.port;

import com.openstrata.admin.domain.model.TenantId;

/** GPU queue SPI (§14.4 / §9.3). Kueue adapter, full profile only. */
public interface GpuQueuePort {
    void createClusterQueue(TenantId tenantId, int gpu);
}
