package cc.openstrata.admin.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cc.openstrata.admin.application.EntitlementGovAppService;
import cc.openstrata.admin.application.ModelGovAppService;
import cc.openstrata.admin.application.ProvisioningAppService;
import cc.openstrata.admin.application.QuotaGovAppService;
import cc.openstrata.admin.application.TenantGovAppService;
import cc.openstrata.admin.application.TenantResourceAppService;
import cc.openstrata.admin.web.AdminTenantController;
import cc.openstrata.admin.application.dto.ComponentApplyRequest;
import cc.openstrata.admin.application.dto.CreateTenantRequest;
import cc.openstrata.admin.application.dto.TenantPatchRequest;
import cc.openstrata.admin.config.TenantContext;
import cc.openstrata.admin.domain.model.PackageTier;
import cc.openstrata.admin.domain.model.TenantGovernance;
import cc.openstrata.admin.domain.model.TenantId;
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
        when(tenantGov.listDetails()).thenReturn(List.of(
            new TenantGovernance(new TenantId("t1"), PackageTier.STANDARD),
            new TenantGovernance(new TenantId("t2"), PackageTier.STANDARD)));
        assertEquals(2, controller.listTenants().size());
        verify(tenantGov).listDetails();
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
        cc.openstrata.admin.domain.model.ProvisioningPlan plan =
            new cc.openstrata.admin.domain.model.ProvisioningPlan("p1", new TenantId("t1"),
                "{}", cc.openstrata.admin.domain.model.ProvisioningStatus.PENDING,
                java.time.Instant.now());
        when(provisioning.apply("t1", new ComponentApplyRequest(Set.of("billing")))).thenReturn(plan);
        assertEquals(plan, controller.applyComponents("t1", new ComponentApplyRequest(Set.of("billing"))));
    }
}
