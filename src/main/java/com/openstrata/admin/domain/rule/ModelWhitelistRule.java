package com.openstrata.admin.domain.rule;

import com.openstrata.admin.domain.RuleResult;
import com.openstrata.admin.domain.model.PackageTier;

/**
 * RULE-04: only Enterprise-tier tenants may be authorized for restricted
 * third-party models (§14.2). Non-Enterprise requests yield
 * {@code MODEL_RESTRICTED} (403).
 */
public class ModelWhitelistRule {

    public RuleResult validate(PackageTier tier, boolean restricted) {
        if (restricted && !tier.isEnterprise()) {
            return RuleResult.fail("MODEL_RESTRICTED",
                "Restricted models require the Enterprise plan (§14.2)");
        }
        return RuleResult.ok();
    }
}
