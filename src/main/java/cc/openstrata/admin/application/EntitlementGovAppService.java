package cc.openstrata.admin.application;

import cc.openstrata.admin.application.dto.EntitlementRequest;
import cc.openstrata.admin.config.TenantContext;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.domain.RuleResult;
import cc.openstrata.admin.domain.model.EntitlementSet;
import cc.openstrata.admin.domain.model.TenantGovernance;
import cc.openstrata.admin.domain.model.TenantId;
import cc.openstrata.admin.domain.port.ControlPlaneClient;
import cc.openstrata.admin.web.ErrorCode;
import org.springframework.stereotype.Service;

/** Use case: set component whitelist (RULE-02 / ADR-0005). DESIGN §4. */
@Service
public class EntitlementGovAppService {

    private final GovernanceAuthorityService governance;
    private final WhitelistManifestService whitelist;
    private final ControlPlaneClient controlPlaneClient;
    private final AuditAggregationService audit;

    public EntitlementGovAppService(GovernanceAuthorityService governance,
                                    WhitelistManifestService whitelist,
                                    ControlPlaneClient controlPlaneClient,
                                    AuditAggregationService audit) {
        this.governance = governance;
        this.whitelist = whitelist;
        this.controlPlaneClient = controlPlaneClient;
        this.audit = audit;
    }

    public TenantGovernance setEntitlements(String tenantId, EntitlementRequest req) {
        EntitlementSet set = req.toEntitlementSet();
        RuleResult result = whitelist.validate(set);
        if (!result.passed()) {
            throw new DomainException(ErrorCode.ENTITLEMENT_DEP_VIOLATION,
                result.violations().get(0).message());
        }
        TenantId id = new TenantId(tenantId);
        controlPlaneClient.setEntitlements(id, set); // single write point (ADR-0001)
        TenantGovernance g = governance.load(id);
        g.applyEntitlements(set);
        governance.save(g);
        audit.record(actor(), cc.openstrata.admin.domain.model.AuditScope.PLATFORM, tenantId,
            "ENTITLEMENTS_SET", java.util.Map.of("components", set.enabledComponents().toString()));
        return g;
    }

    private String actor() {
        String t = TenantContext.tenantId();
        return t == null ? "system" : t;
    }
}
