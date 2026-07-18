package cc.openstrata.admin.domain.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cc.openstrata.admin.domain.model.EntitlementSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EntitlementConsistencyRuleTest {

    private final EntitlementConsistencyRule rule = new EntitlementConsistencyRule();

    @Test
    void billingWithoutMultitenancyViolates() {
        assertFalse(rule.validate(new EntitlementSet(Set.of("billing"))).passed());
    }

    @Test
    void multitenancyWithoutAuthViolates() {
        assertFalse(rule.validate(new EntitlementSet(Set.of("multitenancy"))).passed());
    }

    @Test
    void fullChainSatisfies() {
        assertTrue(rule.validate(new EntitlementSet(
            Set.of("billing", "multitenancy", "auth"))).passed());
    }

    @Test
    void emptyWhitelistOk() {
        assertTrue(rule.validate(new EntitlementSet(Set.of())).passed());
    }
}
