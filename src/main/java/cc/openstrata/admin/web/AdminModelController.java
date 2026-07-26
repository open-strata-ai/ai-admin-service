package cc.openstrata.admin.web;

import cc.openstrata.admin.application.ModelAppService;
import cc.openstrata.admin.application.dto.ModelView;
import cc.openstrata.admin.application.dto.UpsertModelRequest;
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

/** Model catalog REST surface (PR-C). Models are bound to a provider. */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminModelController {

    private final ModelAppService modelApp;

    public AdminModelController(ModelAppService modelApp) {
        this.modelApp = modelApp;
    }

    @GetMapping("/models")
    public List<ModelView> list() {
        return modelApp.list();
    }

    @GetMapping("/providers/{providerId}/models")
    public List<ModelView> listByProvider(@PathVariable String providerId) {
        return modelApp.listByProvider(providerId);
    }

    @PostMapping("/models")
    public ResponseEntity<ModelView> create(@Valid @RequestBody UpsertModelRequest req) {
        requirePlatformAdmin();
        return ResponseEntity.status(201).body(modelApp.create(req));
    }

    @GetMapping("/models/{id}")
    public ModelView get(@PathVariable String id) {
        return modelApp.get(id);
    }

    @PutMapping("/models/{id}")
    public ModelView update(@PathVariable String id,
                            @Valid @RequestBody UpsertModelRequest req) {
        requirePlatformAdmin();
        return modelApp.update(id, req);
    }

    @DeleteMapping("/models/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        requirePlatformAdmin();
        modelApp.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void requirePlatformAdmin() {
        if (!TenantContext.isPlatformAdmin()) {
            throw new DomainException(ErrorCode.FORBIDDEN,
                "platform-admin role required for model writes");
        }
    }
}
