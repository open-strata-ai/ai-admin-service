package cc.openstrata.admin.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA mirror of the {@code models} table (PR-C). A model is a served capability
 * bound to a {@link ProviderEntity} via {@link #providerId}.
 */
@Entity
@Table(name = "models")
public class ModelEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "family", length = 64)
    private String family;

    @Column(name = "context_window")
    private Integer contextWindow;

    @Column(name = "max_output_tokens")
    private Integer maxOutputTokens;

    @Column(name = "capabilities", columnDefinition = "TEXT")
    private String capabilities;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ NOT NULL DEFAULT now()")
    private Instant createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ NOT NULL DEFAULT now()")
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFamily() { return family; }
    public void setFamily(String family) { this.family = family; }
    public Integer getContextWindow() { return contextWindow; }
    public void setContextWindow(Integer contextWindow) { this.contextWindow = contextWindow; }
    public Integer getMaxOutputTokens() { return maxOutputTokens; }
    public void setMaxOutputTokens(Integer maxOutputTokens) { this.maxOutputTokens = maxOutputTokens; }
    public String getCapabilities() { return capabilities; }
    public void setCapabilities(String capabilities) { this.capabilities = capabilities; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
