package com.openstrata.admin.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openstrata.admin.application.dto.ComponentApplyRequest;
import com.openstrata.admin.domain.DomainException;
import com.openstrata.admin.domain.RuleResult;
import com.openstrata.admin.domain.model.ProvisioningPlan;
import com.openstrata.admin.domain.model.ProvisioningStatus;
import com.openstrata.admin.domain.model.TenantId;
import com.openstrata.admin.domain.port.ManifestPort;
import com.openstrata.admin.domain.port.ProvisioningPort;
import com.openstrata.admin.domain.rule.OrchestrationPlanRule;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProvisioningAppServiceTest {

    @Mock ManifestPort manifestPort;
    @Mock ProvisioningPort provisioningPort;
    @Mock OrchestrationConsensusService consensus;
    @Mock OrchestrationPlanRule orchestrationRule;
    @Mock AuditAggregationService audit;
    @InjectMocks ProvisioningAppService service;

    private ProvisioningPlan plan(ProvisioningStatus status) {
        return new ProvisioningPlan("p1", new TenantId("t1"), "{}", status, Instant.now());
    }

    @Test
    void applyResolvesAndProvisions() {
        when(manifestPort.expand(any(), any())).thenReturn(Set.of("billing", "auth"));
        when(orchestrationRule.requireResolverPlan(any(), any())).thenReturn(RuleResult.ok());
        when(consensus.start(any(), any())).thenReturn(plan(ProvisioningStatus.PENDING));
        when(consensus.transition("p1", ProvisioningStatus.APPLYING))
            .thenReturn(plan(ProvisioningStatus.APPLYING));
        service.apply("t1", new ComponentApplyRequest(Set.of("billing")));
        verify(provisioningPort).apply(eq("t1"), any());
    }

    @Test
    void applyRejectsPlanViolation() {
        when(manifestPort.expand(any(), any())).thenReturn(Set.of("billing"));
        when(orchestrationRule.requireResolverPlan(any(), any()))
            .thenReturn(RuleResult.fail("ORCHESTRATION_PLAN_VIOLATION", "missing"));
        assertThrows(DomainException.class,
            () -> service.apply("t1", new ComponentApplyRequest(Set.of("billing"))));
    }
}
