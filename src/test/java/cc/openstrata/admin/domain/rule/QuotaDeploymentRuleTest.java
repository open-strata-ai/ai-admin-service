package cc.openstrata.admin.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cc.openstrata.admin.domain.model.PackageTier;
import cc.openstrata.admin.domain.model.QuotaPolicy;
import cc.openstrata.admin.domain.model.ResourceQuota;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuotaDeploymentRuleTest {

    private final QuotaDeploymentRule rule = new QuotaDeploymentRule();

    @Test
    void trialDefaultsHaveNoGpu() {
        QuotaPolicy p = rule.trialDefaults();
        assertEquals(PackageTier.TRIAL, p.pkg());
        assertEquals(false, p.quota().hasGpu());
    }

    @Test
    void nonFullProfileSkipsKueueGpu() {
        QuotaPolicy p = new QuotaPolicy(PackageTier.ENTERPRISE,
            new ResourceQuota(8, 16, 4, 10_000_000, 500, 8), true);
        List<QuotaDeploymentRule.QuotaDeployment> d = rule.deploy(p, false, false);
        assertEquals(2, d.size()); // k8s + gateway only
        assertTrue(d.stream().noneMatch(x -> x.target().equals("kueue-cluster-queue")));
    }

    @Test
    void fullProfileSelfHostedDeploysKueueGpu() {
        QuotaPolicy p = new QuotaPolicy(PackageTier.ENTERPRISE,
            new ResourceQuota(8, 16, 4, 10_000_000, 500, 8), true);
        List<QuotaDeploymentRule.QuotaDeployment> d = rule.deploy(p, true, true);
        assertEquals(3, d.size());
        assertTrue(d.stream().anyMatch(x -> x.target().equals("kueue-cluster-queue")));
    }

    @Test
    void gpuRequestedButNoGpuQuotaSkipsKueue() {
        QuotaPolicy p = new QuotaPolicy(PackageTier.ENTERPRISE,
            new ResourceQuota(8, 16, 0, 10_000_000, 500, 8), false);
        List<QuotaDeploymentRule.QuotaDeployment> d = rule.deploy(p, true, true);
        assertFalse(d.stream().anyMatch(x -> x.target().equals("kueue-cluster-queue")));
    }
}
