package com.openstrata.admin.application;

import com.openstrata.admin.application.dto.ModelGrantRequest;
import com.openstrata.admin.domain.DomainException;
import com.openstrata.admin.domain.RuleResult;
import com.openstrata.admin.domain.model.ModelWhitelist;
import com.openstrata.admin.domain.model.TenantGovernance;
import com.openstrata.admin.domain.model.TenantId;
import com.openstrata.admin.domain.port.ModelRegistryPort;
import com.openstrata.admin.domain.rule.ModelWhitelistRule;
import com.openstrata.admin.web.ErrorCode;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Use case: grant model vendor access (RULE-04). DESIGN §4. */
@Service
public class ModelGovAppService {

    private final GovernanceAuthorityService governance;
    private final ModelWhitelistRule modelRule;
    private final ModelRegistryPort modelRegistryPort;

    public ModelGovAppService(GovernanceAuthorityService governance,
                              ModelWhitelistRule modelRule,
                              ModelRegistryPort modelRegistryPort) {
        this.governance = governance;
        this.modelRule = modelRule;
        this.modelRegistryPort = modelRegistryPort;
    }

    public TenantGovernance grant(String tenantId, ModelGrantRequest req) {
        TenantGovernance g = governance.load(new TenantId(tenantId));
        RuleResult result = modelRule.validate(g.packageTier(), req.restricted());
        if (!result.passed()) {
            throw new DomainException(ErrorCode.MODEL_RESTRICTED, result.violations().get(0).message());
        }
        modelRegistryPort.authorize(tenantId, req.provider(), req.model(), req.restricted());
        Set<String> allowed = new LinkedHashSet<>();
        allowed.add(req.provider() + "/" + req.model());
        g.applyModelWhitelist(new ModelWhitelist(allowed, req.restricted()));
        return governance.save(g);
    }
}
