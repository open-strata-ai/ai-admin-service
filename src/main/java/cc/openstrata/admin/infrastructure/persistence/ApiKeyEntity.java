package cc.openstrata.admin.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA mirror of the {@code api_keys} table (PR-C). Only a hash of the key is
 * stored — the plaintext is returned to the caller exactly once at creation and
 * never persisted or logged.
 */
@Entity
@Table(name = "api_keys")
public class ApiKeyEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "tenant_id", length = 64)
    private String tenantId;

    @Column(name = "owner_email", length = 255)
    private String ownerEmail;

    @Column(name = "prefix", nullable = false, length = 16)
    private String prefix;

    @Column(name = "key_hash", nullable = false, columnDefinition = "TEXT")
    private String keyHash;

    @Column(name = "scopes", columnDefinition = "TEXT")
    private String scopes;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Column(name = "expires_at", columnDefinition = "TIMESTAMPTZ")
    private Instant expiresAt;

    @Column(name = "last_used_at", columnDefinition = "TIMESTAMPTZ")
    private Instant lastUsedAt;

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ NOT NULL DEFAULT now()")
    private Instant createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ NOT NULL DEFAULT now()")
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }
    public String getScopes() { return scopes; }
    public void setScopes(String scopes) { this.scopes = scopes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
