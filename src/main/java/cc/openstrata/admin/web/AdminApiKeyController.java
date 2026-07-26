package cc.openstrata.admin.web;

import cc.openstrata.admin.application.ApiKeyAppService;
import cc.openstrata.admin.application.dto.ApiKeyGenerated;
import cc.openstrata.admin.application.dto.ApiKeyView;
import cc.openstrata.admin.application.dto.CreateApiKeyRequest;
import cc.openstrata.admin.config.TenantContext;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.web.ErrorCode;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API key lifecycle REST surface (PR-C). The plaintext is returned once at
 *  creation; subsequent reads return only metadata + prefix. */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminApiKeyController {

    private final ApiKeyAppService apiKeyApp;

    public AdminApiKeyController(ApiKeyAppService apiKeyApp) {
        this.apiKeyApp = apiKeyApp;
    }

    @GetMapping("/api-keys")
    public List<ApiKeyView> list() {
        return apiKeyApp.list();
    }

    @GetMapping("/api-keys/{id}")
    public ApiKeyView get(@PathVariable String id) {
        return apiKeyApp.get(id);
    }

    @PostMapping("/api-keys")
    public ResponseEntity<ApiKeyGenerated> create(@Valid @RequestBody CreateApiKeyRequest req) {
        requirePlatformAdmin();
        ApiKeyGenerated generated = apiKeyApp.create(req);
        return ResponseEntity.status(201).body(generated);
    }

    @DeleteMapping("/api-keys/{id}")
    public ResponseEntity<Void> revoke(@PathVariable String id) {
        requirePlatformAdmin();
        apiKeyApp.revoke(id);
        return ResponseEntity.noContent().build();
    }

    private void requirePlatformAdmin() {
        if (!TenantContext.isPlatformAdmin()) {
            throw new DomainException(ErrorCode.FORBIDDEN,
                "platform-admin role required for api-key writes");
        }
    }
}
