package com.openstrata.admin.domain.rule;

import com.openstrata.admin.domain.RuleResult;
import com.openstrata.admin.domain.RuleViolation;
import com.openstrata.admin.domain.model.EntitlementSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RULE-02: component whitelist MUST be compatible with the `PlatformManifest`
 * dependency graph (§12.4). Key chain: `billing` → `multitenancy` → `auth`.
 * Any missing required component yields {@code ENTITLEMENT_DEP_VIOLATION} (422).
 */
public class EntitlementConsistencyRule {

    private static final Map<String, List<String>> DEPS = Map.of(
        "billing", List.of("multitenancy"),
        "multitenancy", List.of("auth")
    );

    public RuleResult validate(EntitlementSet whitelist) {
        List<RuleViolation> violations = new ArrayList<>();
        for (String component : whitelist.enabledComponents()) {
            for (String required : DEPS.getOrDefault(component, List.of())) {
                if (!whitelist.enabledComponents().contains(required)) {
                    violations.add(new RuleViolation("ENTITLEMENT_DEP_VIOLATION",
                        "Enabling '%s' requires '%s' to be enabled first (§12.4)"
                            .formatted(component, required)));
                }
            }
        }
        return RuleResult.of(violations);
    }
}
