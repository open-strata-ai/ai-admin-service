package cc.openstrata.admin.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cc.openstrata.admin.domain.model.EntitlementSet;
import cc.openstrata.admin.domain.model.IsolationSpec;
import cc.openstrata.admin.domain.model.ModelWhitelist;
import cc.openstrata.admin.domain.model.PackageTier;
import cc.openstrata.admin.domain.model.QuotaPolicy;
import cc.openstrata.admin.domain.model.ResourceQuota;
import cc.openstrata.admin.domain.model.TenantGovernance;
import cc.openstrata.admin.domain.model.TenantId;
import java.time.Instant;
import java.util.Set;

/**
 * Maps between the {@link TenantGovernance} domain aggregate and its JPA entity.
 * JSON columns are (de)serialized with Jackson (governance state is schema-less
 * by design — see DESIGN §8).
 */
public final class TenantGovernanceMapper {

    private static final ObjectMapper M = new ObjectMapper();

    /** Empty defaults so an optional governance field never serializes to NULL
     *  (the DB columns are NOT NULL and Hibernate's `update` won't relax them on
     *  an already-created table). RC-9: a freshly created tenant has no quota /
     *  entitlements / whitelist yet, so persist empty, valid JSON instead. */
    private static final QuotaPolicy DEFAULT_QUOTA =
        new QuotaPolicy(PackageTier.STANDARD, new ResourceQuota(0, 0, 0, 0, 0, 0), false);
    private static final EntitlementSet DEFAULT_ENTITLEMENTS = new EntitlementSet(Set.of());
    private static final ModelWhitelist DEFAULT_WHITELIST = new ModelWhitelist(Set.of(), false);

    private TenantGovernanceMapper() {}

    public static TenantGovernanceEntity toEntity(TenantGovernance g) {
        TenantGovernanceEntity e = new TenantGovernanceEntity();
        e.setTenantId(g.tenantId().value());
        e.setPkg(g.packageTier().name());
        try {
            e.setQuotaPolicy(M.writeValueAsString(g.quotaPolicy() != null ? g.quotaPolicy() : DEFAULT_QUOTA));
            e.setEntitlements(M.writeValueAsString(g.entitlements() != null ? g.entitlements() : DEFAULT_ENTITLEMENTS));
            e.setModelWhitelist(M.writeValueAsString(g.modelWhitelist() != null ? g.modelWhitelist() : DEFAULT_WHITELIST));
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
