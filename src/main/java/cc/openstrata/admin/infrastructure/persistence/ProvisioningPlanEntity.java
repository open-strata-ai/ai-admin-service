package cc.openstrata.admin.infrastructure.persistence;

import cc.openstrata.admin.domain.model.ProvisioningStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** JPA mirror of {@code ProvisioningPlan} lifecycle (ADR-3). */
@Entity
@Table(name = "provisioning_plans")
public class ProvisioningPlanEntity {

    @Id
    @Column(name = "plan_id")
    private String planId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "manifest", columnDefinition = "TEXT", nullable = false)
    private String manifest;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProvisioningStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getManifest() { return manifest; }
    public void setManifest(String manifest) { this.manifest = manifest; }
    public ProvisioningStatus getStatus() { return status; }
    public void setStatus(ProvisioningStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
