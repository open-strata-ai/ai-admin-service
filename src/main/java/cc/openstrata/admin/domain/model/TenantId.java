package cc.openstrata.admin.domain.model;

import java.util.Objects;

/** Aggregate identity for a tenant (DESIGN §3 VO). */
public record TenantId(String value) {
    public TenantId {
        Objects.requireNonNull(value, "tenantId must not be null");
    }
}
