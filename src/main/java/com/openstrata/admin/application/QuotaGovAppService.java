package com.openstrata.admin.application;

import com.openstrata.admin.application.dto.QuotaRequest;
import com.openstrata.admin.config.OpenstrataProperties;
import com.openstrata.admin.domain.DomainException;
import com.openstrata.admin.domain.model.QuotaPolicy;
import com.openstrata.admin.domain.model.TenantGovernance;
import com.openstrata.admin.domain.model.TenantId;
import com.openstrata.admin.domain.port.ControlPlaneClient;
import com.openstrata.admin.domain.port.GpuQueuePort;
import com.openstrata.admin.domain.rule.QuotaDeploymentRule;
import com.openstrata.admin.web.ErrorCode;
import org.springframework.stereotype.Service;

/** Use case: assign package + deploy quotas (RULE-01). DESIGN §4. */
@Service
public class QuotaGovAppService {

    private final GovernanceAuthorityService governance;
    private final QuotaDeploymentRule quotaRule;
    private final ControlPlaneClient controlPlaneClient;
    private final GpuQueuePort gpuQueuePort;
    private final OpenstrataProperties props;

    public QuotaGovAppService(GovernanceAuthorityService governance,
                              QuotaDeploymentRule quotaRule,
                              ControlPlaneClient controlPlaneClient,
                              GpuQueuePort gpuQueuePort,
                              OpenstrataProperties props) {
        this.governance = governance;
        this.quotaRule = quotaRule;
        this.controlPlaneClient = controlPlaneClient;
        this.gpuQueuePort = gpuQueuePort;
        this.props = props;
    }

    public TenantGovernance assignPackage(String tenantId, QuotaRequest req) {
        TenantGovernance g = governance.load(new TenantId(tenantId));
        QuotaPolicy policy = req.toPolicy(g.packageTier());

        boolean full = props.getFeatures().isMultiTenantEnabled()
            && props.getFeatures().isBillingViewEnabled();
        if (req.gpu() > 0) {
            // GPU quota only for full profile self-hosted inference (§14.4 D2).
            if (!props.getFeatures().isGpuPoolEnabled() || !full) {
                throw new DomainException(ErrorCode.GPU_POOL_NOT_AVAILABLE,
                    "GPU quota requires the full profile with GPU pool enabled");
            }
        }

        // QuotaDeploymentRule computes the targets (K8s + gateway + optional Kueue).
        quotaRule.deploy(policy, full, true);

        // Single write point to domain authority (ADR-0001).
        controlPlaneClient.updateQuota(new TenantId(tenantId), policy.quota());
        if (req.gpu() > 0) {
            gpuQueuePort.createClusterQueue(new TenantId(tenantId), req.gpu());
        }
        g.applyQuota(policy);
        return governance.save(g);
    }
}
