package cc.openstrata.admin.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cc.openstrata.admin.application.dto.EntitlementRequest;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.domain.RuleResult;
import cc.openstrata.admin.domain.model.PackageTier;
import cc.openstrata.admin.domain.model.TenantGovernance;
import cc.openstrata.admin.domain.model.TenantId;
import cc.openstrata.admin.domain.port.ControlPlaneClient;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntitlementGovAppServiceTest {

    @Mock GovernanceAuthorityService gov;
    @Mock WhitelistManifestService whitelist;
    @Mock ControlPlaneClient client;
    @Mock AuditAggregationService audit;
    @InjectMocks EntitlementGovAppService service;

    @Test
    void validWhitelistWritesDomainAuthorityAndSaves() {
        when(whitelist.validate(any())).thenReturn(RuleResult.ok());
        when(gov.load(any())).thenReturn(new TenantGovernance(new TenantId("t1"), PackageTier.STANDARD));
        service.setEntitlements("t1", new EntitlementRequest(Set.of("auth")));
        verify(client).setEntitlements(any(), any());
        verify(gov).save(any());
    }

    @Test
    void dependencyViolationRejected() {
        when(whitelist.validate(any()))
            .thenReturn(RuleResult.fail("ENTITLEMENT_DEP_VIOLATION", "billing requires multitenancy"));
        assertThrows(DomainException.class,
            () -> service.setEntitlements("t1", new EntitlementRequest(Set.of("billing"))));
    }
}
