package cc.openstrata.admin.infrastructure.persistence;

import cc.openstrata.admin.domain.model.PackageTemplate;
import cc.openstrata.admin.domain.port.PackageTemplateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-backed {@link PackageTemplateRepository} (PA-04). Always-on, matching the
 * admin audit/governance persistence pattern. {@code components} is (de)serialized
 * as a JSON array (portable across Postgres/H2).
 */
@Repository
public class JpaPackageTemplateRepository implements PackageTemplateRepository {

    private static final ObjectMapper M = new ObjectMapper();
    private static final TypeReference<List<String>> LIST_OF_STRING = new TypeReference<>() {};

    private final PackageTemplateJpaRepository repo;

    public JpaPackageTemplateRepository(PackageTemplateJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public PackageTemplate save(PackageTemplate template) {
        repo.save(toEntity(template));
        return template;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PackageTemplate> findById(String id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageTemplate> findAll() {
        return repo.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(String id) {
        repo.deleteById(id);
    }

    private PackageTemplateEntity toEntity(PackageTemplate t) {
        PackageTemplateEntity e = new PackageTemplateEntity();
        e.setId(t.getId());
        e.setName(t.getName());
        e.setTier(t.getTier());
        e.setComponents(writeComponents(t.getComponents()));
        e.setQuotaPolicy(t.getQuotaPolicy());
        return e;
    }

    private PackageTemplate toDomain(PackageTemplateEntity e) {
        return new PackageTemplate(e.getId(), e.getName(), e.getTier(),
            readComponents(e.getComponents()), e.getQuotaPolicy());
    }

    private String writeComponents(List<String> components) {
        try {
            return M.writeValueAsString(components == null ? List.of() : components);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize package template components", ex);
        }
    }

    private List<String> readComponents(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return M.readValue(json, LIST_OF_STRING);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize package template components", ex);
        }
    }
}
