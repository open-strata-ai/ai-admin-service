package cc.openstrata.admin.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA mirror of the {@code TenantGovernance} aggregate (ADR-2). Governance state
 * only — NOT authoritative business data. JSON fields are stored as TEXT
 * (see V1__admin_init.sql); the {@link TenantGovernanceMapper} (de)serializes.
 */
@Entity
@Table(name = "tenant_governance")
public class TenantGovernanceEntity {

    @Id
    @Column(name = "tenant_id")
    private String tenantId;

    @Column(nullable = false)
    private String pkg;

    @Column(name = "quota_policy", columnDefinition = "TEXT")
    private String quotaPolicy;

    @Column(name = "entitlements", columnDefinition = "TEXT")
    private String entitlements;

    @Column(name = "model_whitelist", columnDefinition = "TEXT")
    private String modelWhitelist;

    @Column(name = "isolation_spec", columnDefinition = "TEXT")
    private String isolationSpec;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getPkg() { return pkg; }
    public void setPkg(String pkg) { this.pkg = pkg; }
    public String getQuotaPolicy() { return quotaPolicy; }
    public void setQuotaPolicy(String quotaPolicy) { this.quotaPolicy = quotaPolicy; }
    public String getEntitlements() { return entitlements; }
    public void setEntitlements(String entitlements) { this.entitlements = entitlements; }
    public String getModelWhitelist() { return modelWhitelist; }
    public void setModelWhitelist(String modelWhitelist) { this.modelWhitelist = modelWhitelist; }
    public String getIsolationSpec() { return isolationSpec; }
    public void setIsolationSpec(String isolationSpec) { this.isolationSpec = isolationSpec; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
