package cc.openstrata.admin.domain.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import java.util.Objects;

/**
 * Aggregate root: tenant governance (ADR-2). Holds the package assignment,
 * quota policy, component whitelist, model whitelist and isolation spec.
 * Consistency boundary: package must match profile; entitlements must satisfy
 * the manifest dependency graph (validated by {@code EntitlementConsistencyRule}).
 *
 * <p>This service does NOT own authoritative business data — that lives in
 * `ai-platform-api`. This aggregate is the locally-mirrored governance state.
 */
public class TenantGovernance {

    private final TenantId tenantId;
    private PackageTier packageTier;
    private QuotaPolicy quotaPolicy;
    private EntitlementSet entitlements;
    private ModelWhitelist modelWhitelist;
    private IsolationSpec isolationSpec;

    public TenantGovernance(TenantId tenantId, PackageTier packageTier) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.packageTier = Objects.requireNonNull(packageTier, "packageTier");
    }

    public TenantId tenantId() { return tenantId; }
    public PackageTier packageTier() { return packageTier; }

    /** RC-10: expose the real tenant id + package tier to the admin portal so the
     *  registry shows genuine plan data instead of fabricated defaults. */
    @JsonGetter("id")
    public String tenantIdValue() { return tenantId.value(); }

    @JsonGetter("packageTier")
    public String packageTierName() { return packageTier == null ? null : packageTier.name(); }
    public QuotaPolicy quotaPolicy() { return quotaPolicy; }
    public EntitlementSet entitlements() { return entitlements; }
    public ModelWhitelist modelWhitelist() { return modelWhitelist; }
    public IsolationSpec isolationSpec() { return isolationSpec; }

    public void applyPackage(PackageTier tier) {
        this.packageTier = Objects.requireNonNull(tier);
    }

    public void applyQuota(QuotaPolicy policy) {
        this.quotaPolicy = Objects.requireNonNull(policy);
    }

    public void applyEntitlements(EntitlementSet entitlements) {
        this.entitlements = Objects.requireNonNull(entitlements);
    }

    public void applyModelWhitelist(ModelWhitelist whitelist) {
        this.modelWhitelist = Objects.requireNonNull(whitelist);
    }

    public void applyIsolation(IsolationSpec spec) {
        this.isolationSpec = Objects.requireNonNull(spec);
    }
}
