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
import cc.openstrata.admin.config.TenantContext;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.domain.model.ProvisioningPlan;
import cc.openstrata.admin.domain.model.TenantGovernance;
import cc.openstrata.admin.web.ErrorCode;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public List<TenantGovernance> listTenants() {
        return tenantGov.listDetails();
    }

    @PostMapping("/tenants")
    public TenantGovernance createTenant(@Valid @RequestBody CreateTenantRequest req) {
        requirePlatformAdmin();
        return tenantGov.create(req);
    }

    @DeleteMapping("/tenants/{tenantId}")
    public ResponseEntity<Void> deleteTenant(@PathVariable String tenantId) {
        requirePlatformAdmin();
        tenantGov.delete(tenantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tenants/{tenantId}")
    public TenantGovernance getTenant(@PathVariable String tenantId) {
        return tenantGov.get(tenantId);
    }

    @PatchMapping("/tenants/{tenantId}")
    public ResponseEntity<Void> patchTenant(@PathVariable String tenantId,
                                            @Valid @RequestBody TenantPatchRequest req) {
        requirePlatformAdmin();
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

    /** RC-11: tenant governance writes require the platform-admin role. In
     *  dev-mode the interceptor already resolves platform-admin, so local flows
     *  pass; non-dev requests without the role are rejected with 403. */
    private void requirePlatformAdmin() {
        if (!TenantContext.isPlatformAdmin()) {
            throw new DomainException(ErrorCode.FORBIDDEN,
                "platform-admin role required for tenant governance writes");
        }
    }
}
