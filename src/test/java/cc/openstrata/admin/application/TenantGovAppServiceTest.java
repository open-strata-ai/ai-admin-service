package cc.openstrata.admin.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cc.openstrata.admin.application.dto.CreateTenantRequest;
import cc.openstrata.admin.config.OpenstrataProperties;
import cc.openstrata.admin.domain.model.IsolationSpec;
import cc.openstrata.admin.domain.model.PackageTier;
import cc.openstrata.admin.domain.model.TenantGovernance;
import cc.openstrata.admin.domain.model.TenantId;
import cc.openstrata.admin.domain.port.MultiTenancyPort;
import cc.openstrata.admin.domain.rule.IsolationEnforcementRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantGovAppServiceTest {

    @Mock GovernanceAuthorityService gov;
    @Mock IsolationEnforcementRule isolationRule;
    @Mock MultiTenancyPort multiTenancyPort;
    @Mock OpenstrataProperties props;
    @Mock OpenstrataProperties.Features features;
    @InjectMocks TenantGovAppService service;

    @BeforeEach
    void setup() {
        when(props.getFeatures()).thenReturn(features);
    }

    @Test
    void singleTenantSkipsIsolationDeployment() {
        when(features.isMultiTenantEnabled()).thenReturn(false);
        when(gov.createTenant(any(), any()))
            .thenReturn(new TenantGovernance(new TenantId("t1"), PackageTier.STANDARD));
        service.create(new CreateTenantRequest("t1", "standard"));
        verify(gov).createTenant(any(), eq(PackageTier.STANDARD));
        verify(multiTenancyPort, never()).deployQuota(any(), any());
    }

    @Test
    void multiTenantDeploysIsolation() {
        when(features.isMultiTenantEnabled()).thenReturn(true);
        when(features.isBillingViewEnabled()).thenReturn(false);
        IsolationSpec spec = new IsolationSpec("ns",
            new cc.openstrata.admin.domain.model.ResourceQuota(0, 0, 0, 0, 0, 0),
            cc.openstrata.admin.domain.model.NetworkPolicy.DENY_ALL, null,
            new cc.openstrata.admin.domain.model.CollectionPrefix("t1_"),
            new cc.openstrata.admin.domain.model.Bucket("t1-data"));
        when(isolationRule.build(any(), anyBoolean())).thenReturn(spec);
        when(gov.createTenant(any(), any()))
            .thenReturn(new TenantGovernance(new TenantId("t1"), PackageTier.STANDARD));
        service.create(new CreateTenantRequest("t1", "standard"));
        verify(multiTenancyPort).deployQuota(any(), any());
    }
}
