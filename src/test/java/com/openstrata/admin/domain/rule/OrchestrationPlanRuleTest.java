package com.openstrata.admin.domain.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class OrchestrationPlanRuleTest {

    private final OrchestrationPlanRule rule = new OrchestrationPlanRule();

    @Test
    void allRequestedComponentsPresentInPlan() {
        assertTrue(rule.requireResolverPlan(
            Set.of("billing"), Set.of("billing", "multitenancy", "auth")).passed());
    }

    @Test
    void missingComponentViolates() {
        assertFalse(rule.requireResolverPlan(
            Set.of("unknown"), Set.of("billing")).passed());
    }
}
