package com.openstrata.admin.application;

import com.openstrata.admin.config.OpenstrataProperties;
import com.openstrata.admin.domain.DomainException;
import com.openstrata.admin.domain.RuleResult;
import com.openstrata.admin.domain.model.TenantId;
import com.openstrata.admin.domain.rule.GpuPoolEnabledRule;
import com.openstrata.admin.domain.port.GpuQueuePort;
import com.openstrata.admin.web.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * ADR-0002 — GPU pool management timing.
 *
 * <p>The GPU pool is only available in the `full` profile with self-hosted
 * inference (§14.4 D2). The GPU quota view is hidden/disabled otherwise.
 * Conservative default: feature off until explicitly enabled. GPU quota is only
 * deployed to Kueue when the {@link GpuPoolEnabledRule} passes.
 */
@Service
public class GpuPoolManagementService {

    public record GpuPoolView(boolean enabled, String reason, int availableGpu) {}

    private final GpuPoolEnabledRule rule = new GpuPoolEnabledRule();
    private final OpenstrataProperties props;
    private final GpuQueuePort gpuQueuePort;

    public GpuPoolManagementService(OpenstrataProperties props, GpuQueuePort gpuQueuePort) {
        this.props = props;
        this.gpuQueuePort = gpuQueuePort;
    }

    public GpuPoolView view(int availableGpu) {
        boolean full = props.getFeatures().isMultiTenantEnabled()
            && props.getFeatures().isBillingViewEnabled(); // "full" = advanced+ caps
        RuleResult r = rule.evaluate(props.getFeatures().isGpuPoolEnabled(), full,
            props.getFeatures().isModelRegistryEnabled());
        return new GpuPoolView(r.passed(), r.passed() ? "ok" : firstReason(r), availableGpu);
    }

    public void deployGpuQuota(TenantId tenantId, int gpu) {
        boolean full = props.getFeatures().isMultiTenantEnabled()
            && props.getFeatures().isBillingViewEnabled();
        RuleResult r = rule.evaluate(props.getFeatures().isGpuPoolEnabled(), full,
            props.getFeatures().isModelRegistryEnabled());
        if (!r.passed()) {
            throw new DomainException(ErrorCode.GPU_POOL_NOT_AVAILABLE, firstReason(r));
        }
        gpuQueuePort.createClusterQueue(tenantId, gpu);
    }

    private String firstReason(RuleResult r) {
        return r.violations().isEmpty() ? "ok" : r.violations().get(0).message();
    }
}
