package cc.openstrata.admin.web;

import cc.openstrata.admin.application.SsoAppService;
import cc.openstrata.admin.application.dto.SsoConfigRequest;
import cc.openstrata.admin.application.dto.SsoConfigView;
import cc.openstrata.admin.config.TenantContext;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.web.ErrorCode;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** SSO / OIDC IdP configuration REST surface (PR-C). */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminSsoController {

    private final SsoAppService ssoApp;

    public AdminSsoController(SsoAppService ssoApp) {
        this.ssoApp = ssoApp;
    }

    @GetMapping("/sso/config")
    public SsoConfigView getConfig() {
        return ssoApp.getConfig();
    }

    @PutMapping("/sso/config")
    public SsoConfigView saveConfig(@Valid @RequestBody SsoConfigRequest req) {
        requirePlatformAdmin();
        return ssoApp.save(req);
    }

    @PostMapping("/sso/test")
    public Map<String, Object> testConnection() {
        return ssoApp.testConnection();
    }

    @PostMapping("/sso/sync")
    public Map<String, Object> sync() {
        requirePlatformAdmin();
        return ssoApp.sync();
    }

    private void requirePlatformAdmin() {
        if (!TenantContext.isPlatformAdmin()) {
            throw new DomainException(ErrorCode.FORBIDDEN,
                "platform-admin role required for sso writes");
        }
    }
}
