package cc.openstrata.admin.infrastructure.adapter;

import cc.openstrata.admin.domain.model.TenantId;
import cc.openstrata.admin.domain.port.CostPort;
import org.springframework.stereotype.Component;

/** In-memory CostPort standing in for ai-billing-service + OpenCost. */
@Component
public class InMemoryCostAdapter implements CostPort {

    @Override
    public double getTenantCost(TenantId tenantId) {
        return 0.0;
    }
}
