package com.openstrata.admin.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openstrata.admin.domain.model.EntitlementSet;
import com.openstrata.admin.domain.model.IsolationSpec;
import com.openstrata.admin.domain.model.ModelWhitelist;
import com.openstrata.admin.domain.model.PackageTier;
import com.openstrata.admin.domain.model.QuotaPolicy;
import com.openstrata.admin.domain.model.TenantGovernance;
import com.openstrata.admin.domain.model.TenantId;
import java.time.Instant;

/**
 * Maps between the {@link TenantGovernance} domain aggregate and its JPA entity.
 * JSON columns are (de)serialized with Jackson (governance state is schema-less
 * by design — see DESIGN §8).
 */
public final class TenantGovernanceMapper {

    private static final ObjectMapper M = new ObjectMapper();

    private TenantGovernanceMapper() {}

    public static TenantGovernanceEntity toEntity(TenantGovernance g) {
        TenantGovernanceEntity e = new TenantGovernanceEntity();
        e.setTenantId(g.tenantId().value());
        e.setPkg(g.packageTier().name());
        try {
            if (g.quotaPolicy() != null) e.setQuotaPolicy(M.writeValueAsString(g.quotaPolicy()));
            if (g.entitlements() != null) e.setEntitlements(M.writeValueAsString(g.entitlements()));
            if (g.modelWhitelist() != null) e.setModelWhitelist(M.writeValueAsString(g.modelWhitelist()));
            if (g.isolationSpec() != null) e.setIsolationSpec(M.writeValueAsString(g.isolationSpec()));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize governance state", ex);
        }
        e.setUpdatedAt(Instant.now());
        return e;
    }

    public static TenantGovernance toDomain(TenantGovernanceEntity e) {
        TenantGovernance g = new TenantGovernance(new TenantId(e.getTenantId()), PackageTier.valueOf(e.getPkg()));
        try {
            if (e.getQuotaPolicy() != null) {
                g.applyQuota(M.readValue(e.getQuotaPolicy(), QuotaPolicy.class));
            }
            if (e.getEntitlements() != null) {
                g.applyEntitlements(M.readValue(e.getEntitlements(), EntitlementSet.class));
            }
            if (e.getModelWhitelist() != null) {
                g.applyModelWhitelist(M.readValue(e.getModelWhitelist(), ModelWhitelist.class));
            }
            if (e.getIsolationSpec() != null) {
                g.applyIsolation(M.readValue(e.getIsolationSpec(), IsolationSpec.class));
            }
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize governance state", ex);
        }
        return g;
    }
}
