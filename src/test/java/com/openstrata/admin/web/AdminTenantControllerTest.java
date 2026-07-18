package com.openstrata.admin.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openstrata.admin.application.EntitlementGovAppService;
import com.openstrata.admin.application.ModelGovAppService;
import com.openstrata.admin.application.ProvisioningAppService;
import com.openstrata.admin.application.QuotaGovAppService;
import com.openstrata.admin.application.TenantGovAppService;
import com.openstrata.admin.application.TenantResourceAppService;
import com.openstrata.admin.web.AdminTenantController;
import com.openstrata.admin.application.dto.ComponentApplyRequest;
import com.openstrata.admin.application.dto.CreateTenantRequest;
import com.openstrata.admin.application.dto.TenantPatchRequest;
import com.openstrata.admin.config.TenantContext;
import com.openstrata.admin.domain.model.PackageTier;
import com.openstrata.admin.domain.model.TenantGovernance;
import com.openstrata.admin.domain.model.TenantId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminTenantControllerTest {

    private final TenantGovAppService tenantGov = mock(TenantGovAppService.class);
    private final QuotaGovAppService quotaGov = mock(QuotaGovAppService.class);
    private final EntitlementGovAppService entitlementGov = mock(EntitlementGovAppService.class);
    private final ModelGovAppService modelGov = mock(ModelGovAppService.class);
    private final TenantResourceAppService tenantResource = mock(TenantResourceAppService.class);
    private final ProvisioningAppService provisioning = mock(ProvisioningAppService.class);

    private final AdminTenantController controller = new AdminTenantController(
        tenantGov, quotaGov, entitlementGov, modelGov, tenantResource, provisioning);

    @BeforeEach
    void setContext() {
        TenantContext.set(new TenantContext.Tenant("local", Set.of("platform-admin"), true));
    }

    @AfterEach
    void clear() {
        TenantContext.clear();
    }

    @Test
    void listTenantsDelegates() {
        when(tenantGov.list()).thenReturn(List.of("t1", "t2"));
        assertEquals(List.of("t1", "t2"), controller.listTenants());
    }

    @Test
    void createTenantDelegates() {
        TenantGovernance g = new TenantGovernance(new TenantId("t1"), PackageTier.STANDARD);
        when(tenantGov.create(new CreateTenantRequest("t1", "standard"))).thenReturn(g);
        assertEquals(g, controller.createTenant(new CreateTenantRequest("t1", "standard")));
    }

    @Test
    void patchSuspendDelegates() {
        controller.patchTenant("t1", new TenantPatchRequest("suspend"));
        verify(tenantGov).suspend("t1");
    }

    @Test
    void applyComponentsDelegates() {
        com.openstrata.admin.domain.model.ProvisioningPlan plan =
            new com.openstrata.admin.domain.model.ProvisioningPlan("p1", new TenantId("t1"),
                "{}", com.openstrata.admin.domain.model.ProvisioningStatus.PENDING,
                java.time.Instant.now());
        when(provisioning.apply("t1", new ComponentApplyRequest(Set.of("billing")))).thenReturn(plan);
        assertEquals(plan, controller.applyComponents("t1", new ComponentApplyRequest(Set.of("billing"))));
    }
}
