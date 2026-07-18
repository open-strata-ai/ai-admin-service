package cc.openstrata.admin.application;

import cc.openstrata.admin.application.dto.TenantResourceResponse;
import cc.openstrata.admin.domain.model.ResourceQuota;
import cc.openstrata.admin.domain.model.TenantId;
import cc.openstrata.admin.domain.port.ControlPlaneClient;
import cc.openstrata.admin.domain.port.CostPort;
import cc.openstrata.admin.domain.port.MultiTenancyPort;
import org.springframework.stereotype.Service;

/** Use case: tenant resource portrait (multi-source aggregation). DESIGN §4. */
@Service
public class TenantResourceAppService {

    private final ControlPlaneClient controlPlaneClient;
    private final MultiTenancyPort multiTenancyPort;
    private final CostPort costPort;

    public TenantResourceAppService(ControlPlaneClient controlPlaneClient,
                                    MultiTenancyPort multiTenancyPort,
                                    CostPort costPort) {
        this.controlPlaneClient = controlPlaneClient;
        this.multiTenancyPort = multiTenancyPort;
        this.costPort = costPort;
    }

    public TenantResourceResponse view(String tenantId) {
        TenantId id = new TenantId(tenantId);
        ResourceQuota allocated = controlPlaneClient.getProfile(id).allocated();
        ResourceQuota used = multiTenancyPort.queryUsage(id);
        double cost = costPort.getTenantCost(id);
        return new TenantResourceResponse(tenantId, allocated, used, cost);
    }
}
