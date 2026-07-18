package com.openstrata.admin.infrastructure.adapter;

import com.openstrata.admin.domain.model.TenantId;
import com.openstrata.admin.domain.port.GpuQueuePort;
import org.springframework.stereotype.Component;

/** In-memory GpuQueuePort standing in for Kueue (full profile only). */
@Component
public class InMemoryGpuQueueAdapter implements GpuQueuePort {

    @Override
    public void createClusterQueue(TenantId tenantId, int gpu) {
        // Kueue ClusterQueue created here
    }
}
