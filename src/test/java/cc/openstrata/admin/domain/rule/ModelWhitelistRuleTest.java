package cc.openstrata.admin.domain.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cc.openstrata.admin.domain.model.PackageTier;
import org.junit.jupiter.api.Test;

class ModelWhitelistRuleTest {

    private final ModelWhitelistRule rule = new ModelWhitelistRule();

    @Test
    void standardTierRestrictedModelRejected() {
        assertFalse(rule.validate(PackageTier.STANDARD, true).passed());
    }

    @Test
    void enterpriseTierRestrictedModelAllowed() {
        assertTrue(rule.validate(PackageTier.ENTERPRISE, true).passed());
    }

    @Test
    void nonRestrictedModelAlwaysAllowed() {
        assertTrue(rule.validate(PackageTier.TRIAL, false).passed());
    }
}
