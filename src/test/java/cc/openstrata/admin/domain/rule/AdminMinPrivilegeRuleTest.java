package cc.openstrata.admin.domain.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AdminMinPrivilegeRuleTest {

    private final AdminMinPrivilegeRule rule = new AdminMinPrivilegeRule();

    @Test
    void tenantAdminOnOwnTenantOk() {
        assertTrue(rule.enforce("t1", "t1", false).passed());
    }

    @Test
    void tenantAdminOnOtherTenantViolates() {
        assertFalse(rule.enforce("t1", "t2", false).passed());
    }

    @Test
    void platformAdminCrossTenantOk() {
        assertTrue(rule.enforce("t1", "t2", true).passed());
    }
}
