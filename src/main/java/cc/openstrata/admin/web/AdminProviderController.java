package cc.openstrata.admin.web;

import cc.openstrata.admin.application.ProviderAppService;
import cc.openstrata.admin.application.dto.ProviderView;
import cc.openstrata.admin.application.dto.SecretRequest;
import cc.openstrata.admin.application.dto.UpsertProviderRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Provider registry REST surface (PR-C). Standard REST verbs: POST create,
 *  PUT update, GET read, DELETE remove. The credential is set via a dedicated
 *  PUT …/secret endpoint and never returned in any response. */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminProviderController {

    private final ProviderAppService providerApp;

    public AdminProviderController(ProviderAppService providerApp) {
        this.providerApp = providerApp;
    }

    @GetMapping("/providers")
    public List<ProviderView> list() {
        return providerApp.list();
    }

    @PostMapping("/providers")
    public ResponseEntity<ProviderView> create(@Valid @RequestBody UpsertProviderRequest req) {
        requirePlatformAdmin();
        return ResponseEntity.status(201).body(providerApp.create(req));
    }

    @GetMapping("/providers/{id}")
    public ProviderView get(@PathVariable String id) {
        return providerApp.get(id);
    }

    @PutMapping("/providers/{id}")
    public ProviderView update(@PathVariable String id,
                               @Valid @RequestBody UpsertProviderRequest req) {
        requirePlatformAdmin();
        return providerApp.update(id, req);
    }

    @DeleteMapping("/providers/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        requirePlatformAdmin();
        providerApp.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/providers/{id}/secret")
    public ResponseEntity<Void> setSecret(@PathVariable String id,
                                          @Valid @RequestBody SecretRequest req) {
        requirePlatformAdmin();
        providerApp.setSecret(id, req.secret());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/providers/{id}/secret")
    public ResponseEntity<Void> deleteSecret(@PathVariable String id) {
        requirePlatformAdmin();
        providerApp.deleteSecret(id);
        return ResponseEntity.ok().build();
    }

    private void requirePlatformAdmin() {
        if (!TenantContext.isPlatformAdmin()) {
            throw new DomainException(ErrorCode.FORBIDDEN,
                "platform-admin role required for provider writes");
        }
    }
}
