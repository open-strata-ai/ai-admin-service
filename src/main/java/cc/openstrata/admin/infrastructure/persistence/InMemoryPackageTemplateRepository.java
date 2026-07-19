package cc.openstrata.admin.infrastructure.persistence;

import cc.openstrata.admin.domain.model.PackageTemplate;
import cc.openstrata.admin.domain.port.PackageTemplateRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/** In-memory PackageTemplateRepository (H2, Batch H2). */
@Repository
public class InMemoryPackageTemplateRepository implements PackageTemplateRepository {
    private final Map<String, PackageTemplate> store = new ConcurrentHashMap<>();

    @Override
    public PackageTemplate save(PackageTemplate template) {
        store.put(template.getId(), template);
        return template;
    }

    @Override
    public Optional<PackageTemplate> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<PackageTemplate> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }
}
