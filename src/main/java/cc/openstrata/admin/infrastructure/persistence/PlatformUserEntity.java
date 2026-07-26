package cc.openstrata.admin.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA mirror of the {@code platform_users} table (PR-C). Local user directory
 * used by the admin console. Roles include {@code viewer} (added in M1). Actual
 * auth/SSO is delegated to the IdP via {@code AuthPort}; this row is the
 * governance record that the console reads and writes.
 */
@Entity
@Table(name = "platform_users")
public class PlatformUserEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "name", length = 128)
    private String name;

    @Column(name = "tenant_id", length = 64)
    private String tenantId;

    @Column(name = "roles", columnDefinition = "TEXT")
    private String roles;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ NOT NULL DEFAULT now()")
    private Instant createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ NOT NULL DEFAULT now()")
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
