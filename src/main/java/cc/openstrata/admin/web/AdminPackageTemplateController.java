package cc.openstrata.admin.web;

import cc.openstrata.admin.application.PackageTemplateAppService;
import cc.openstrata.admin.application.dto.CreatePackageTemplateRequest;
import cc.openstrata.admin.application.dto.PackageTemplateResponse;
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

/** Package template REST surface (PA-04, Batch H2). */
@RestController
@RequestMapping("/api/v1/admin/package-templates")
public class AdminPackageTemplateController {
    private final PackageTemplateAppService service;

    public AdminPackageTemplateController(PackageTemplateAppService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PackageTemplateResponse> create(@Valid @RequestBody CreatePackageTemplateRequest req) {
        return ResponseEntity.status(201).body(PackageTemplateResponse.from(service.create(req)));
    }

    @GetMapping
    public List<PackageTemplateResponse> list() {
        return service.list().stream().map(PackageTemplateResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PackageTemplateResponse get(@PathVariable String id) {
        return PackageTemplateResponse.from(service.get(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
