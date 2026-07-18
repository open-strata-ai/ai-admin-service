package com.openstrata.admin.domain.rule;

import com.openstrata.admin.domain.RuleResult;

/**
 * RULE-05: platform-level vs tenant-level role scope is strictly separated
 * (§14.3). A tenant admin cannot operate on another tenant's data without an
 * explicit scope; platform admins may act across tenants.
 */
public class AdminMinPrivilegeRule {

    public RuleResult enforce(String actorTenantId, String targetTenantId,
                              boolean platformAdmin) {
        if (!platformAdmin && !actorTenantId.equals(targetTenantId)) {
            return RuleResult.fail("SCOPE_VIOLATION",
                "Tenant admin cannot operate on other tenants without an explicit scope (§14.3)");
        }
        return RuleResult.ok();
    }
}
