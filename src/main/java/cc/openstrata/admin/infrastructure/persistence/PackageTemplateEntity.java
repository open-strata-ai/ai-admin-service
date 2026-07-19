package cc.openstrata.admin.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA mirror of the {@code package_templates} table (PA-04, V1__admin_init.sql).
 * {@code components} is stored as a JSON array in a TEXT column (portable across
 * Postgres/H2); the {@link JpaPackageTemplateRepository} (de)serializes it.
 */
@Entity
@Table(name = "package_templates")
public class PackageTemplateEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String tier;

    @Column(name = "components", columnDefinition = "TEXT", nullable = false)
    private String components;

    @Column(name = "quota_policy", columnDefinition = "TEXT")
    private String quotaPolicy;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public String getComponents() { return components; }
    public void setComponents(String components) { this.components = components; }
    public String getQuotaPolicy() { return quotaPolicy; }
    public void setQuotaPolicy(String quotaPolicy) { this.quotaPolicy = quotaPolicy; }
}
