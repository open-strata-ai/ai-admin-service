package cc.openstrata.admin.domain.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cc.openstrata.admin.domain.model.IsolationSpec;
import cc.openstrata.admin.domain.model.NetworkPolicy;
import cc.openstrata.admin.domain.model.TenantId;
import org.junit.jupiter.api.Test;

class IsolationEnforcementRuleTest {

    private final IsolationEnforcementRule rule = new IsolationEnforcementRule();

    @Test
    void buildProducesDenyAllIsolation() {
        IsolationSpec spec = rule.build(new TenantId("t1"), false);
        assertTrue(spec.networkPolicy().denyAll());
        assertTrue(spec.collectionPrefix().value().startsWith("t1"));
    }

    @Test
    void multiTenantWithDenyAllPasses() {
        IsolationSpec spec = rule.build(new TenantId("t1"), false);
        assertTrue(rule.enforce(spec, true).passed());
    }

    @Test
    void multiTenantWithoutDenyAllViolates() {
        IsolationSpec spec = new IsolationSpec("ns",
            new cc.openstrata.admin.domain.model.ResourceQuota(0, 0, 0, 0, 0, 0),
            new NetworkPolicy(false), null,
            new cc.openstrata.admin.domain.model.CollectionPrefix("t1_"),
            new cc.openstrata.admin.domain.model.Bucket("t1-data"));
        assertFalse(rule.enforce(spec, true).passed());
    }

    @Test
    void singleTenantSkipsIsolationCheck() {
        IsolationSpec spec = rule.build(new TenantId("t1"), false);
        assertTrue(rule.enforce(spec, false).passed());
    }
}
