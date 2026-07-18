package cc.openstrata.admin.web;

import cc.openstrata.admin.application.EntitlementGovAppService;
import cc.openstrata.admin.application.ModelGovAppService;
import cc.openstrata.admin.application.ProvisioningAppService;
import cc.openstrata.admin.application.QuotaGovAppService;
import cc.openstrata.admin.application.TenantGovAppService;
import cc.openstrata.admin.application.TenantResourceAppService;
import cc.openstrata.admin.application.dto.ComponentApplyRequest;
import cc.openstrata.admin.application.dto.CreateTenantRequest;
import cc.openstrata.admin.application.dto.EntitlementRequest;
import cc.openstrata.admin.application.dto.ModelGrantRequest;
import cc.openstrata.admin.application.dto.QuotaRequest;
import cc.openstrata.admin.application.dto.TenantPatchRequest;
import cc.openstrata.admin.application.dto.TenantResourceResponse;
import cc.openstrata.admin.domain.model.ProvisioningPlan;
import cc.openstrata.admin.domain.model.TenantGovernance;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Tenant governance REST surface (ADR-0001/0003/0005). DESIGN 14.6. */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminTenantController {

    private final TenantGovAppService tenantGov;
    private final QuotaGovAppService quotaGov;
    private final EntitlementGovAppService entitlementGov;
    private final ModelGovAppService modelGov;
    private final TenantResourceAppService tenantResource;
    private final ProvisioningAppService provisioning;

    public AdminTenantController(TenantGovAppService tenantGov, QuotaGovAppService quotaGov,
                                 EntitlementGovAppService entitlementGov, ModelGovAppService modelGov,
                                 TenantResourceAppService tenantResource, ProvisioningAppService provisioning) {
        this.tenantGov = tenantGov;
        this.quotaGov = quotaGov;
        this.entitlementGov = entitlementGov;
        this.modelGov = modelGov;
        this.tenantResource = tenantResource;
        this.provisioning = provisioning;
    }

    @GetMapping("/tenants")
    public List<String> listTenants() {
        return tenantGov.list();
    }

    @PostMapping("/tenants")
    public TenantGovernance createTenant(@Valid @RequestBody CreateTenantRequest req) {
        return tenantGov.create(req);
    }

    @GetMapping("/tenants/{tenantId}")
    public TenantGovernance getTenant(@PathVariable String tenantId) {
        return tenantGov.get(tenantId);
    }

    @PatchMapping("/tenants/{tenantId}")
    public ResponseEntity<Void> patchTenant(@PathVariable String tenantId,
                                            @Valid @RequestBody TenantPatchRequest req) {
        if (req.isSuspend()) {
            tenantGov.suspend(tenantId);
        } else if (req.isResume()) {
            tenantGov.resume(tenantId);
        } else {
            throw new IllegalArgumentException("unsupported action: " + req.action());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tenants/{tenantId}/quotas")
    public TenantGovernance assignQuota(@PathVariable String tenantId,
                                        @Valid @RequestBody QuotaRequest req) {
        return quotaGov.assignPackage(tenantId, req);
    }

    @PostMapping("/tenants/{tenantId}/entitlements")
    public TenantGovernance setEntitlements(@PathVariable String tenantId,
                                            @Valid @RequestBody EntitlementRequest req) {
        return entitlementGov.setEntitlements(tenantId, req);
    }

    @PostMapping("/tenants/{tenantId}/model-grants")
    public TenantGovernance grantModel(@PathVariable String tenantId,
                                       @Valid @RequestBody ModelGrantRequest req) {
        return modelGov.grant(tenantId, req);
    }

    @GetMapping("/tenants/{tenantId}/resources")
    public TenantResourceResponse resources(@PathVariable String tenantId) {
        return tenantResource.view(tenantId);
    }

    @PostMapping("/tenants/{tenantId}/components:apply")
    public ProvisioningPlan applyComponents(@PathVariable String tenantId,
                                           @Valid @RequestBody ComponentApplyRequest req) {
        return provisioning.apply(tenantId, req);
    }
}
