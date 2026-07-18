package com.openstrata.admin.domain.model;

/**
 * Subscribed package / plan tier (DESIGN §3 VO). Maps to the `package` column.
 * `ENTERPRISE` is the only tier permitted to authorize restricted models
 * (RULE-04 / §14.2) and represents the "full" self-hosted deployment tier.
 */
public enum PackageTier {
    TRIAL,
    STANDARD,
    ENTERPRISE;

    public boolean isEnterprise() {
        return this == ENTERPRISE;
    }
}
