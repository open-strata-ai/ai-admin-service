package cc.openstrata.admin.infrastructure.persistence;

import cc.openstrata.admin.domain.model.PackageTemplate;
import cc.openstrata.admin.domain.port.PackageTemplateRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link PackageTemplateRepository} — retained as a lightweight test
 * double for unit tests. NOT a Spring bean: the runtime binding is the always-on
 * {@link JpaPackageTemplateRepository} so package templates survive restarts (PA-04).
 */
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
