# ai-admin-service · AI Coding Rules & Skills (SKILLS)

> **Source**: Extracted from `docs/DESIGN.md` §5 (Domain Rules), §11 (Integration Points), §12 (Security & Multi-tenancy). These rules guide AI-assisted development within this repo.

---

## RULE-01: Quota Deployment with Profile-Gated GPU

| Aspect | Detail |
| --- | --- |
| **Trigger** | Assigning a plan/package with quota deployment (`QuotaGovAppService.assignPackage()`). |
| **Constraint** | Quotas map to K8s ResourceQuota + Kueue ClusterQueue + gateway `tenant×model` quotas. `advanced` profile governs CPU/Token/QPS/Vector by default. **GPU quota is only effective for `full` profile with self-hosted inference** (§8.1 D5, §14.4 D2). Non-applicable dimensions are silently skipped. |
| **Rationale** | GPU pools only exist in full deployments; deploying GPU quotas without infrastructure causes configuration drift. |

**Implementation pattern**:
```java
// Domain service: QuotaDeploymentRule
public List<QuotaDeployment> deploy(QuotaPolicy policy, PackageAssignment pkg) {
    List<QuotaDeployment> deployments = new ArrayList<>();
    deployments.addAll(buildK8sQuotas(policy));        // CPU/Mem/Token/QPS/Vector
    if (pkg.isFull() && policy.hasGpu()) {
        deployments.add(buildKueueClusterQueue(policy.getGpu()));  // GPU only full
    }
    return deployments;
}
```

**Test checklist**:
- [ ] Enterprise plan, advanced profile → no GPU quota deployed.
- [ ] Enterprise plan, full profile → GPU quota deployed to Kueue.
- [ ] Trial plan → correct trial resource limits.

---

## RULE-02: Entitlement Dependency Graph Validation

| Aspect | Detail |
| --- | --- |
| **Trigger** | Setting component whitelist (`EntitlementGovAppService.set()`). |
| **Constraint** | Component whitelist MUST be compatible with `PlatformManifest` dependency graph (§12.4 table). Key rules: `billing` requires `multitenancy`; `multitenancy` requires `auth`. Violations return `ENTITLEMENT_DEP_VIOLATION` (HTTP 422). |
| **Rationale** | Prevents enabling billing without multi-tenant isolation, or multi-tenancy without authentication. |

**Implementation pattern**:
```java
// Domain service: EntitlementConsistencyRule
public void validate(Set<ComponentKey> whitelist, PlatformManifest manifest) {
    Map<ComponentKey, Set<ComponentKey>> deps = manifest.getDependencyGraph();
    for (ComponentKey comp : whitelist) {
        Set<ComponentKey> required = deps.getOrDefault(comp, Set.of());
        for (ComponentKey req : required) {
            if (!whitelist.contains(req)) {
                throw new EntitlementDepViolationException(comp, req);
            }
        }
    }
}
```

**Test checklist**:
- [ ] `billing` enabled, `multitenancy` disabled → `422 ENTITLEMENT_DEP_VIOLATION`.
- [ ] `multitenancy` enabled, `auth` disabled → `422 ENTITLEMENT_DEP_VIOLATION`.
- [ ] Valid dependency chain → accepted.

---

## RULE-03: Isolation Enforcement — Data Never Leaves Tenant

| Aspect | Detail |
| --- | --- |
| **Trigger** | Applying `TenantGovernance` or provisioning isolation carriers. |
| **Constraint** | Enforce "data never leaves tenant": Namespace + NetworkPolicy (deny-all) + data prefix (CollectionPrefix for Milvus, Bucket for MinIO) per tenant (§14.2, §8.2). Isolation is mandatory for multi-tenant tenants. |
| **Rationale** | Core security property for multi-tenant SaaS; network isolation + data prefix prevents cross-tenant leakage at infrastructure level. |

**Implementation pattern**:
```java
// Domain service: IsolationEnforcementRule
public IsolationSpec buildIsolation(TenantId tenantId) {
    return new IsolationSpec(
        new Namespace("ai-tenant-" + tenantId),
        NetworkPolicy.DENY_ALL,
        new CollectionPrefix(tenantId + "_"),
        new Bucket(tenantId + "-data"),
        // optional: KueueQueue for full profile
    );
}
```

---

## RULE-04: Model Whitelist — Enterprise-Only Restricted Models

| Aspect | Detail |
| --- | --- |
| **Trigger** | Granting model vendor access (`ModelGovAppService.grant()`). |
| **Constraint** | Only Enterprise-tier tenants may be authorized for restricted third-party models (e.g., GPT-4o) (§14.2). Non-Enterprise tenants receive `403 MODEL_RESTRICTED`. |
| **Rationale** | Vendor licensing and cost profiles restrict premium models to Enterprise plan. |

---

## RULE-05: Admin Minimum Privilege Enforcement

| Aspect | Detail |
| --- | --- |
| **Trigger** | Any admin operation (create/suspend tenant, set quotas, grant models). |
| **Constraint** | Platform-level vs. tenant-level role scope is STRICTLY separated (§14.3). Platform admins cannot modify other tenants' data unless explicitly scoped. Admin operations require MFA (§14.6). All admin changes are fully audited (even if `security` profile is off). |
| **Rationale** | Prevents privilege escalation and cross-tenant admin abuse. |

**Implementation pattern**:
```java
@PreAuthorize("hasRole('platform-admin')")
public void createTenant(CreateTenantCommand cmd) { ... }

@PreAuthorize("hasRole('platform-admin') or " +
    "(hasRole('tenant-admin') and #tenantId == authentication.tenantId)")
public void setQuotas(String tenantId, QuotaPolicy policy) { ... }

// MFA enforcement via annotation
@RequireMfa
public void deleteTenant(String tenantId) { ... }
```

---

## RULE-06: Orchestration Closed-Loop via Dependency Resolver

| Aspect | Detail |
| --- | --- |
| **Trigger** | `ProvisioningAppService.apply()` — component change trigger. |
| **Constraint** | Component changes MUST go through: (1) `ai-dependency-resolver` → dependency graph expansion → incremental plan (add/reuse/remove); (2) `ai-provisioning-engine` (ArgoCD) → execution. Plan status tracked in `provisioning_plans` table (PENDING → APPLYING → DONE/FAILED). |
| **Rationale** | Prevents manual infrastructure drift; ensures all dependencies are satisfied before deployment. |

**Implementation pattern**:
```java
// Application service: ProvisioningAppService
public ProvisioningPlan apply(String tenantId, ComponentChange change) {
    // 1. Resolve dependencies
    IncrementalPlan plan = dependencyResolver.expand(tenantId, change);
    // 2. Execute via ArgoCD
    DeploymentResult result = provisioningEngine.apply(plan);
    // 3. Track status
    return provisioningPlanRepository.save(new ProvisioningPlan(tenantId, plan, "APPLYING"));
}
```

**Test checklist**:
- [ ] Adding `billing` component → resolver includes `multitenancy` as dependency.
- [ ] Plan fails → status = FAILED, retryable.
- [ ] Plan succeeds → status = DONE after ArgoCD sync confirmation.

---

## RULE-07: Integration with Platform-API (ControlPlaneClient)

| Aspect | Detail |
| --- | --- |
| **Trigger** | Any governance operation that writes domain data (tenant, user, plan, quota, entitlements). |
| **Constraint** | All domain writes MUST go through `ControlPlaneClient` → `ai-platform-api`. Admin-service NEVER writes directly to platform-api's database. The ACL layer maps governance DTOs ⇄ platform-api domain objects. |
| **Rationale** | `ai-platform-api` is the single authoritative write path; prevents dual-write inconsistencies. |

**Checklist**:
- [ ] `createTenant()` → calls `platformApi.createTenant()` via `ControlPlaneClient`.
- [ ] `setQuotas()` → calls `platformApi.updateQuota()` via `ControlPlaneClient`.
- [ ] ACL mapping tested: admin DTO → platform domain object → platform DTO.

---

## RULE-08: Tenant Data Isolation (Admin Governance)

| Aspect | Detail |
| --- | --- |
| **Trigger** | Any admin data access or governance operation. |
| **Constraint** | Tenant data isolation is carried by Capsule/K8s (§8.2 matrix). This service's own `tenant_governance` table uses `tenant_id` column isolation + RLS. Audit log is full-retention (even if `security` is off). Platform admins cannot access other tenants' data without explicit scope. |
| **Rationale** | Governance data is sensitive; multi-tenant admin portal must enforce strict isolation. |

---

## RULE-09: Immutable Audit Trail (Admin)

| Aspect | Detail |
| --- | --- |
| **Trigger** | Any governance change (tenant create/suspend, quota set, entitlement change, user sync). |
| **Constraint** | All governance changes write to `audit_log` via `AuditEntry` aggregate (immutable, INSERT-ONLY). Audit is mandatory regardless of `security` profile (§14.6). Contains: actor, scope (platform/tenant), tenant_id, action, payload, timestamp. |
| **Rationale** | Compliance requirement; platform administrators' actions must be fully auditable. |

**Implementation pattern**:
```java
// AuditEntry entity: INSERT-ONLY, no UPDATE or DELETE allowed
@Entity
@Table(name = "audit_log")
public class AuditEntryEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(nullable = false) private String actor;
    @Column(nullable = false) private String scope;    // platform / tenant
    private String tenantId;
    @Column(nullable = false) private String action;
    @Column(columnDefinition = "JSONB") private String payload;
    @Column(nullable = false) private Instant createdAt;
    // No setters for id, createdAt; no @PreUpdate
}
```

---

## RULE-10: SPI Contract & Multi-Target Orchestration

| Aspect | Detail |
| --- | --- |
| **Trigger** | Service startup or governance operation. |
| **Constraint** | Readiness depends on: PostgreSQL → Keycloak → Redis → `ai-platform-api`. Additional dependencies (Capsule, Kueue, billing, ModelRegistry) are conditional on profile features. All SPI contracts must be tested against `bom.yaml` `interface_versions`. |
| **Rationale** | Admin-service orchestrates 9+ downstream targets; each must be verified available before accepting traffic. |

**Checklist**:
- [ ] `bom.yaml` versions match all SPI targets.
- [ ] `AuthPort` contract test (Keycloak).
- [ ] `ControlPlaneClient` contract test (ai-platform-api).
- [ ] `MultiTenancyPort` contract test (Capsule, when enabled).
- [ ] `CachePort` contract test (Redis + Valkey).
- [ ] `ManifestPort` contract test (ai-dependency-resolver).
- [ ] `CostPort` contract test (ai-billing-service + OpenCost).

---

> **References**: Full domain rules in `docs/DESIGN.md` §5, §11, §12. Cross-reference `ai-platform-api/docs/SKILLS.md` for shared rules (entitlement dependency, tenant isolation, audit).
