package com.openstrata.admin.domain.rule;

import com.openstrata.admin.domain.RuleResult;
import com.openstrata.admin.domain.model.PackageTier;
import com.openstrata.admin.domain.model.QuotaPolicy;
import com.openstrata.admin.domain.model.ResourceQuota;
import java.util.ArrayList;
import java.util.List;

/**
 * RULE-01: map plan quotas to K8s ResourceQuota + Kueue ClusterQueue + gateway
 * `tenant×model` quotas. GPU quota is only effective for the `full` profile with
 * self-hosted inference (§8.1 D5 / §14.4 D2); non-applicable dimensions are
 * silently skipped.
 */
public class QuotaDeploymentRule {

    public record QuotaDeployment(String target, String detail) {}

    public List<QuotaDeployment> deploy(QuotaPolicy policy, boolean fullProfile,
                                        boolean selfHostedInference) {
        List<QuotaDeployment> deployments = new ArrayList<>();
        ResourceQuota q = policy.quota();
        deployments.add(new QuotaDeployment("k8s-resource-quota",
            "cpu=%d mem=%dGi".formatted(q.cpuCores(), q.memoryGi())));
        deployments.add(new QuotaDeployment("gateway-tenant-model-quota",
            "token=%d qps=%d vector=%d".formatted(q.tokenPerMonth(), q.qps(), q.vectorCount())));
        if (fullProfile && selfHostedInference && q.hasGpu()) {
            deployments.add(new QuotaDeployment("kueue-cluster-queue",
                "gpu=%d".formatted(q.gpu())));
        }
        return deployments;
    }

    /** Trial tier default limits (RULE-01 test checklist). */
    public QuotaPolicy trialDefaults() {
        return new QuotaPolicy(PackageTier.TRIAL,
            new ResourceQuota(2, 4, 0, 100_000, 50, 1), false);
    }
}
