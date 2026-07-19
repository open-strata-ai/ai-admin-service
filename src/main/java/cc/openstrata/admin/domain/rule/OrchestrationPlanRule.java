package cc.openstrata.admin.domain.rule;

import cc.openstrata.admin.domain.RuleResult;
import cc.openstrata.admin.domain.RuleViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * RULE-06: every component change MUST pass through the dependency resolver
 * (ai-dependency-resolver) to produce an incremental plan before provisioning.
 * Validates that all requested components appear in the resolved plan.
 */
@org.springframework.stereotype.Component
public class OrchestrationPlanRule {

    public RuleResult requireResolverPlan(Set<String> requestedComponents,
                                          Set<String> resolvedPlan) {
        List<RuleViolation> violations = new ArrayList<>();
        for (String component : requestedComponents) {
            if (!resolvedPlan.contains(component)) {
                violations.add(new RuleViolation("ORCHESTRATION_PLAN_VIOLATION",
                    "Component '%s' is absent from the resolver plan".formatted(component)));
            }
        }
        return RuleResult.of(violations);
    }
}
