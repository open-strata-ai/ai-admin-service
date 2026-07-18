package cc.openstrata.admin.domain.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GpuPoolEnabledRuleTest {

    private final GpuPoolEnabledRule rule = new GpuPoolEnabledRule();

    @Test
    void featureDisabledNotAvailable() {
        assertFalse(rule.evaluate(false, true, true).passed());
    }

    @Test
    void notFullProfileNotAvailable() {
        assertFalse(rule.evaluate(true, false, true).passed());
    }

    @Test
    void noSelfHostedNotAvailable() {
        assertFalse(rule.evaluate(true, true, false).passed());
    }

    @Test
    void fullProfileSelfHostedAvailable() {
        assertTrue(rule.evaluate(true, true, true).passed());
    }
}
