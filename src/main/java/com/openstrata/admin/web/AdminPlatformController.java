package com.openstrata.admin.web;

import com.openstrata.admin.application.AuditAppService;
import com.openstrata.admin.application.GlobalResourceAppService;
import com.openstrata.admin.application.GpuPoolManagementService;
import com.openstrata.admin.application.UserGovAppService;
import com.openstrata.admin.application.dto.GlobalResourceResponse;
import com.openstrata.admin.application.dto.UserSyncRequest;
import com.openstrata.admin.domain.model.AuditEntry;
import com.openstrata.admin.domain.model.AuditScope;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminPlatformController {

    private final GlobalResourceAppService globalResource;
    private final GpuPoolManagementService gpuPool;
    private final UserGovAppService userGov;
    private final AuditAppService audit;

    public AdminPlatformController(GlobalResourceAppService globalResource,
                                   GpuPoolManagementService gpuPool,
                                   UserGovAppService userGov, AuditAppService audit) {
        this.globalResource = globalResource;
        this.gpuPool = gpuPool;
        this.userGov = userGov;
        this.audit = audit;
    }

    @GetMapping("/global-resources")
    public GlobalResourceResponse globalResources() {
        return globalResource.view();
    }

    @GetMapping("/gpu-pool")
    public GpuPoolManagementService.GpuPoolView gpuPool() {
        return gpuPool.view(0);
    }

    @GetMapping("/users")
    public Set<String> users(@RequestParam String tenantId) {
        return userGov.list(tenantId);
    }

    @PostMapping("/users:sync")
    public ResponseEntity<Void> syncUsers(@Valid @RequestBody UserSyncRequest req) {
        userGov.sync(req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/audit")
    public List<AuditEntry> audit(@RequestParam(required = false) String scope,
                                  @RequestParam(required = false) String tenantId) {
        AuditScope auditScope = scope == null ? null : AuditScope.valueOf(scope.toUpperCase());
        return audit.query(auditScope, tenantId);
    }
}
