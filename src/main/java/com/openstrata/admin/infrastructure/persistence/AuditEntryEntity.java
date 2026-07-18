package com.openstrata.admin.infrastructure.persistence;

import com.openstrata.admin.domain.model.AuditScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Immutable audit record (RULE-09 / §14.6). INSERT-ONLY: no setters for
 * {@code id} / {@code createdAt}, no update path. Audit is mandatory regardless
 * of the security profile.
 */
@Entity
@Table(name = "audit_log")
public class AuditEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditScope scope;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(nullable = false)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public AuditScope getScope() { return scope; }
    public void setScope(AuditScope scope) { this.scope = scope; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
