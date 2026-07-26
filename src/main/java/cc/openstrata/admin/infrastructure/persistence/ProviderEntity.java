package cc.openstrata.admin.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA mirror of the {@code providers} table (PR-C). Provider credentials are
 * NOT stored here — only {@link #secretRef}, an opaque reference to the secret
 * in the configured secret store (Vault by default).
 */
@Entity
@Table(name = "providers")
public class ProviderEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "base_url", columnDefinition = "TEXT")
    private String baseUrl;

    @Column(name = "auth_type", nullable = false)
    private String authType = "api_key";

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Column(name = "secret_ref", length = 255)
    private String secretRef;

    @Column(name = "labels", columnDefinition = "TEXT")
    private String labels;

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ NOT NULL DEFAULT now()")
    private Instant createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ NOT NULL DEFAULT now()")
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSecretRef() { return secretRef; }
    public void setSecretRef(String secretRef) { this.secretRef = secretRef; }
    public String getLabels() { return labels; }
    public void setLabels(String labels) { this.labels = labels; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
