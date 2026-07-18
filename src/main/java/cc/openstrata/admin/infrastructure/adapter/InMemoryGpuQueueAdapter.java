package cc.openstrata.admin.infrastructure.adapter;

import cc.openstrata.admin.domain.model.TenantId;
import cc.openstrata.admin.domain.port.GpuQueuePort;
import org.springframework.stereotype.Component;

/** In-memory GpuQueuePort standing in for Kueue (full profile only). */
@Component
public class InMemoryGpuQueueAdapter implements GpuQueuePort {

    @Override
    public void createClusterQueue(TenantId tenantId, int gpu) {
        // Kueue ClusterQueue created here
    }
}
