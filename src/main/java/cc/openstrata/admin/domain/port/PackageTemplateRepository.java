package cc.openstrata.admin.domain.port;

import cc.openstrata.admin.domain.model.PackageTemplate;
import java.util.List;
import java.util.Optional;

/** Persistence SPI for package templates (PA-04). */
public interface PackageTemplateRepository {
    PackageTemplate save(PackageTemplate template);

    Optional<PackageTemplate> findById(String id);

    List<PackageTemplate> findAll();

    void delete(String id);
}
