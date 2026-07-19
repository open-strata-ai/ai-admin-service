package cc.openstrata.admin.application;

import cc.openstrata.admin.application.dto.CreatePackageTemplateRequest;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.domain.model.PackageTemplate;
import cc.openstrata.admin.domain.port.PackageTemplateRepository;
import cc.openstrata.admin.web.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;

/** Use case: package template CRUD (PA-04, Batch H2). */
@Service
public class PackageTemplateAppService {
    private final PackageTemplateRepository repository;

    public PackageTemplateAppService(PackageTemplateRepository repository) {
        this.repository = repository;
    }

    public PackageTemplate create(CreatePackageTemplateRequest req) {
        String id = "pt-" + req.name().toLowerCase().replaceAll("\\s+", "-");
        PackageTemplate t = new PackageTemplate(id, req.name(), req.tier(), req.components(), req.quotaPolicy());
        return repository.save(t);
    }

    public PackageTemplate get(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new DomainException(ErrorCode.PACKAGE_TEMPLATE_NOT_FOUND,
                "package template not found: " + id));
    }

    public List<PackageTemplate> list() {
        return repository.findAll();
    }

    public void delete(String id) {
        repository.delete(id);
    }
}
