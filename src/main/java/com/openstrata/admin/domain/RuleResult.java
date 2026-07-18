package com.openstrata.admin.domain;

import java.util.List;

/**
 * Result of a pure domain-rule evaluation: a boolean `passed` flag plus the list
 * of {@link RuleViolation}s (empty when passed). Domain rules are side-effect
 * free so they are trivially unit-testable (DESIGN §15).
 */
public record RuleResult(boolean passed, List<RuleViolation> violations) {

    public static RuleResult ok() {
        return new RuleResult(true, List.of());
    }

    public static RuleResult fail(String code, String message) {
        return new RuleResult(false, List.of(new RuleViolation(code, message)));
    }

    public static RuleResult of(List<RuleViolation> violations) {
        return new RuleResult(violations.isEmpty(), violations);
    }
}
