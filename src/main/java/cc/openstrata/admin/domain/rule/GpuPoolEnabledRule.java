package cc.openstrata.admin.domain.rule;

import cc.openstrata.admin.domain.RuleResult;

/**
 * ADR-0002 (GPU pool management timing): the GPU pool is only available in the
 * `full` profile with self-hosted inference. Otherwise it is hidden/disabled
 * (§14.4 D2). Conservative default: feature off until explicitly enabled.
 */
@org.springframework.stereotype.Component
public class GpuPoolEnabledRule {

    public RuleResult evaluate(boolean gpuPoolFeatureEnabled, boolean fullProfile,
                               boolean selfHostedInference) {
        if (!gpuPoolFeatureEnabled) {
            return RuleResult.fail("GPU_POOL_NOT_AVAILABLE",
                "GPU pool feature is not enabled");
        }
        if (!fullProfile) {
            return RuleResult.fail("GPU_POOL_NOT_AVAILABLE",
                "GPU pool is only available in the full profile");
        }
        if (!selfHostedInference) {
            return RuleResult.fail("GPU_POOL_NOT_AVAILABLE",
                "GPU pool is only for self-hosted inference");
        }
        return RuleResult.ok();
    }
}
