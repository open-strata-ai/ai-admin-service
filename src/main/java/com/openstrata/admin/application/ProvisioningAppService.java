package com.openstrata.admin.application;

import com.openstrata.admin.application.dto.ComponentApplyRequest;
import com.openstrata.admin.config.TenantContext;
import com.openstrata.admin.domain.DomainException;
import com.openstrata.admin.domain.RuleResult;
import com.openstrata.admin.domain.model.ProvisioningPlan;
import com.openstrata.admin.domain.model.ProvisioningStatus;
import com.openstrata.admin.domain.model.TenantId;
import com.openstrata.admin.domain.port.ManifestPort;
import com.openstrata.admin.domain.port.ProvisioningPort;
import com.openstrata.admin.domain.rule.OrchestrationPlanRule;
import com.openstrata.admin.web.ErrorCode;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Use case: trigger component-change orchestration (RULE-06 / ADR-0003). */
@Service
public class ProvisioningAppService {

    private final ManifestPort manifestPort;
    private final ProvisioningPort provisioningPort;
    private final OrchestrationConsensusService consensus;
    private final OrchestrationPlanRule orchestrationRule;
    private final AuditAggregationService audit;

    public ProvisioningAppService(ManifestPort manifestPort,
                                  ProvisioningPort provisioningPort,
                                  OrchestrationConsensusService consensus,
                                  OrchestrationPlanRule orchestrationRule,
                                  AuditAggregationService audit) {
        this.manifestPort = manifestPort;
        this.provisioningPort = provisioningPort;
        this.consensus = consensus;
        this.orchestrationRule = orchestrationRule;
        this.audit = audit;
    }

    public ProvisioningPlan apply(String tenantId, ComponentApplyRequest req) {
        TenantId id = new TenantId(tenantId);
        // 1. Resolve dependency graph → incremental plan (RULE-06).
        Set<String> resolved = manifestPort.expand(id, req.components());
        RuleResult result = orchestrationRule.requireResolverPlan(req.components(), resolved);
        if (!result.passed()) {
            throw new DomainException(ErrorCode.ORCHESTRATION_PLAN_VIOLATION,
                result.violations().get(0).message());
        }
        // 2. Track plan lifecycle (ADR-0003) and execute via provisioning engine.
        ProvisioningPlan plan = consensus.start(id, resolved.toString());
        plan = consensus.transition(plan.planId(), ProvisioningStatus.APPLYING);
        provisioningPort.apply(tenantId, plan.manifest());
        audit.record(actor(), com.openstrata.admin.domain.model.AuditScope.PLATFORM, tenantId,
            "MANIFEST_SYNCED", Map.of("planId", plan.planId()));
        return plan;
    }

    private String actor() {
        String t = TenantContext.tenantId();
        return t == null ? "system" : t;
    }
}
