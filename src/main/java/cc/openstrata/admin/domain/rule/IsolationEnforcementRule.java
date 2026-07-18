package cc.openstrata.admin.domain.rule;

import cc.openstrata.admin.domain.RuleResult;
import cc.openstrata.admin.domain.RuleViolation;
import cc.openstrata.admin.domain.model.Bucket;
import cc.openstrata.admin.domain.model.CollectionPrefix;
import cc.openstrata.admin.domain.model.IsolationSpec;
import cc.openstrata.admin.domain.model.KueueQueue;
import cc.openstrata.admin.domain.model.NetworkPolicy;
import cc.openstrata.admin.domain.model.ResourceQuota;
import cc.openstrata.admin.domain.model.TenantId;
import java.util.ArrayList;
import java.util.List;

/**
 * RULE-03: enforce "data never leaves tenant" — Namespace + NetworkPolicy
 * (deny-all) + per-tenant data prefix/bucket (§14.2 / §8.2). Mandatory for
 * multi-tenant tenants; builds the {@link IsolationSpec} deterministically.
 */
public class IsolationEnforcementRule {

    public RuleResult enforce(IsolationSpec spec, boolean multiTenant) {
        List<RuleViolation> violations = new ArrayList<>();
        if (multiTenant) {
            if (spec.networkPolicy() == null || !spec.networkPolicy().denyAll()) {
                violations.add(new RuleViolation("ISOLATION_VIOLATION",
                    "NetworkPolicy must be deny-all for multi-tenant isolation"));
            }
            if (spec.collectionPrefix() == null) {
                violations.add(new RuleViolation("ISOLATION_VIOLATION",
                    "CollectionPrefix required for tenant data isolation"));
            }
            if (spec.bucket() == null) {
                violations.add(new RuleViolation("ISOLATION_VIOLATION",
                    "Bucket required for tenant data isolation"));
            }
        }
        return RuleResult.of(violations);
    }

    public IsolationSpec build(TenantId tenantId, boolean fullProfile) {
        KueueQueue gpuQueue = fullProfile
            ? new KueueQueue("ai-tenant-" + tenantId.value() + "-gpu", 0) : null;
        return new IsolationSpec(
            "ai-tenant-" + tenantId.value(),
            new ResourceQuota(0, 0, 0, 0, 0, 0),
            NetworkPolicy.DENY_ALL,
            gpuQueue,
            new CollectionPrefix(tenantId.value() + "_"),
            new Bucket(tenantId.value() + "-data"));
    }
}
