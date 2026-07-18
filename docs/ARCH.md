# ai-admin-service · Architecture Decision Document (ARCH)

> **Source**: Extracted from `docs/DESIGN.md` §1, §2, §3, §6. Full design doc is the authority; this distillate captures architectural decisions, constraints, and SPI boundaries for implementers.

---

## 1. Service Identity

| Attribute | Value |
| --- | --- |
| Domain | control-plane |
| Language / Framework | Java · Spring Boot 3.x (Jakarta Persistence) |
| Optional | No — core, enabled in all profiles (starter~full) |
| Default Port | 8088 |
| Platform Version | v1.0.0 |
| Deployment | 2 replicas, `ai-system` namespace, 500m CPU / 1Gi request |
| Database | PostgreSQL@16.0 (core base), schema `admin_gov` |
| Data Authority | NO — does NOT hold authoritative business data (that's `ai-platform-api`) |

---

## 2. Bounded Context

`ai-admin-service` is OpenStrata's **overall admin Portal backend** (Architecture Doc §14, added in v2.2). It serves **platform administrators and tenant administrators**, providing unified governance over: tenants, users, platform global resources, and tenant-specific resources. It materializes governance decisions into isolation carriers (Capsule/K8s/Kueue/Keycloak) and tenant-level `PlatformManifest` (§12).

This service is the **governance orchestrator**, NOT the data authority. It dispatches intent across multiple managed planes but does not own the authoritative data.

### 2.1 Profile-Gated Governance Dimensions

| Profile | admin | gpuPool | multiTenant | billingView | Capsule | Kueue |
| --- | --- | --- | --- | --- | --- | --- |
| starter | true | false | false | false | — | — |
| standard | true | false | false | false | — | — |
| advanced | true | false | true | true | injected | — |
| full | true | true | true | true | injected | injected |

### 2.2 Upstream Consumer

| Consumer | Auth Type | Role |
| --- | --- | --- |
| `ai-admin-frontend` (TypeScript + antd) | Bearer JWT + X-Tenant-Id | Admin governance UI |

### 2.3 Downstream Managed Planes (9 targets)

| Target | Governance Action | Profile |
| --- | --- | --- |
| `ai-platform-api` | Domain authority writes (tenant, user, plan, quota, entitlements) | All |
| Capsule / K8s | Namespace + NetworkPolicy + ResourceQuota isolation | Advanced/Full |
| Kueue | GPU ClusterQueue management | Full only |
| Keycloak | User/role synchronization (SSO, LDAP, AD) | All |
| OpenCost + `ai-billing-service` | Cost data for dashboards | Advanced/Full |
| `ai-dependency-resolver` (Go) | Dependency graph expansion → incremental plan | All |
| `ai-provisioning-engine` (Go) | ArgoCD deployment execution | All |
| ModelRegistry | Model supply/authorization | All |

### 2.4 Boundary Rules
- **Inbound**: Authenticated via `Auth` SPI (Keycloak OIDC/JWT); `X-Tenant-Id` injected by gateway; platform-level vs. tenant-level role scope STRICTLY separated.
- **Out-of-scope (NEVER in this service)**: Holding authoritative business data (in `ai-platform-api`), direct database writes to other services, agent execution.
- **Outbound**: ALL calls go through SPI Ports with ACL anti-corruption layers. Orchestrates 9 downstream targets.
- **Data**: Persists only governance orchestration state (`tenant_governance`, `provisioning_plans`, `audit_log`). Resource views are runtime aggregations (not persisted).
- **Relationship with ai-platform-api**: Admin-service is the governance orchestrator; platform-api is the domain authority. Admin-service calls platform-api for all data writes. NEVER writes directly to platform-api's database.

---

## 3. Responsibility Matrix

### 3.1 Core Capabilities

| Capability | Description | Arch § | Critical Rules |
| --- | --- | --- | --- |
| Tenant Management | Lifecycle CRUD, plan assignment, suspend/resume, isolation configuration. | §14.2 / §8.1 | All writes go through `ControlPlaneClient` → ai-platform-api |
| User Management | SSO/LDAP/AD integration, RBAC four roles, lifecycle sync. | §14.3 / §4.7.3 | Keycloak user/role sync via AuthPort |
| Global Resource Management | Cluster nodes, GPU pools, shared services, global quotas, platform cost, capacity planning. | §14.4 | Runtime aggregation from Capsule/K8s/OpenCost |
| Tenant Resource Management | Allocated vs used resources, isolation carriers, invoices (Showback/Chargeback). | §14.5 / §8.3 | Multi-source aggregation: platform-api + Capsule + billing |
| Quota Provisioning | Plan quotas → K8s ResourceQuota + Kueue ClusterQueue + gateway quotas. | §8.2 / §14.5 | GPU quota only for full profile (§8.1 D5) |
| Component Scope | Tenant-allowable component whitelist → `PlatformManifest.spec`. | §12.1 / §14.2 | Must pass §12.4 dependency validation |
| Vendor Authorization | Per-tenant third-party model grants. | §14.2 | Enterprise-only for restricted models |
| Security & Audit | Admin MFA, least privilege, immutable audit trail. | §14.6 / §4.7.4 | Audit even when security=off |
| Orchestration Closed-Loop | Dependency graph → incremental plan → ArgoCD provisioning. | §13.3 | Plan tracked in `provisioning_plans` (PENDING→APPLYING→DONE/FAILED) |

### 3.2 Application Layer Use Cases (from §4)

Primarily **orchestration use cases** — dispatch governance intent to multiple managed planes. CQRS: write orchestration commands, read multi-source aggregation projections.

| Use Case | App Service | Type | Domain Event |
| --- | --- | --- | --- |
| Create/suspend tenant (governance) | `TenantGovAppService.create/suspend()` | Orchestration | `TenantGovernanceApplied` |
| Assign plan + quotas | `QuotaGovAppService.assignPackage()` | Orchestration | `QuotaDeployed` |
| Set component whitelist | `EntitlementGovAppService.set()` | Orchestration | `EntitlementDeployed` |
| Grant model vendor | `ModelGovAppService.grant()` | Orchestration | `EntitlementDeployed` |
| User SSO/lifecycle sync | `UserGovAppService.syncLifecycle()` | Orchestration | `UserProvisioned` |
| Global resource view | `GlobalResourceAppService.view()` | Read (multi-source) | — |
| Tenant resource profile | `TenantResourceAppService.view()` | Read (multi-source) | — |
| Trigger component change orchestration | `ProvisioningAppService.apply()` | Orchestration | `ManifestSynced` |
| Audit query | `AuditAppService.query()` | Read | — |

---

## 4. Domain Model

### 4.1 Architecture Style

DDD four-layer architecture. Application layer is primarily **orchestration** — dispatch to platform-api, Capsule, Kueue, Keycloak, dependency-resolver, provisioning-engine. CQRS separates orchestration commands from multi-source read aggregates.

**Critical distinction**: This service does NOT own the authoritative business data model. The authoritative domain model lives in `ai-platform-api`. This service owns only governance orchestration state.

### 4.2 Aggregate Design

| Aggregate Root | Consistency Boundary | Persisted? | Key Invariants |
| --- | --- | --- | --- |
| `TenantGovernance` | `QuotaPolicy` + `EntitlementSet` + `ModelWhitelist` + `IsolationSpec`. | Yes (tenant_governance table) | Package assignment must match profile; entitlements must satisfy dependency graph. |
| `GlobalResourceView` | `Node` list + `GpuPool` + `GlobalQuotaBudget`. | No (runtime aggregation) | Snapshot from Capsule/K8s/OpenCost at query time. |
| `TenantResourceView` | `Allocated` + `Used` + `IsolationSpec` + `Bill`. | No (runtime aggregation) | Multi-source: platform-api (allocated) + Capsule (used) + billing (cost). |
| `AuditEntry` | Standalone, immutable. | Yes (audit_log, INSERT-ONLY) | actor, scope (platform/tenant), action, payload, timestamp. Never updated or deleted. |

### 4.3 Entities (Identity-Based)

| Entity | Identity Field | Key Attributes |
| --- | --- | --- |
| `TenantGovernance` | `TenantId` | package (enum), quota_policy (JSONB), entitlements (JSONB), model_whitelist (JSONB), isolation_spec (JSONB) |
| `ProvisioningPlan` | `PlanId` | tenantId, manifest (JSONB), status (enum: PENDING/APPLYING/DONE/FAILED) |
| `QuotaPolicy` | (embedded) | CPU/Mem/GPU/Token/QPS/Vector limits |
| `EntitlementSet` | (embedded) | Set of `ComponentKey` (gateway, rag, srs, billing, ...) |
| `ModelWhitelist` | (embedded) | Set of `{provider, model}` pairs |
| `IsolationSpec` | (embedded) | Namespace, NetworkPolicy, KueueQueue, CollectionPrefix, Bucket |

### 4.4 Value Objects (Immutable)

| VO | Type | Constraints |
| --- | --- | --- |
| `TenantId`, `ClusterId`, `AuditId` | String | UUID format |
| `PackageAssignment` | Enum | `Trial`, `Standard`, `Enterprise` |
| `ResourceQuota` | Object | `{cpu, mem, gpu, token, qps, vector}` limits |
| `NetworkPolicy` | Object | Namespace-level network rules; default DENY_ALL |
| `KueueQueue` | Object | GPU ClusterQueue configuration (only full profile) |
| `CollectionPrefix` | String | Milvus collection prefix per tenant (e.g., `{tenantId}_`) |
| `Bucket` | String | MinIO bucket per tenant (e.g., `{tenantId}-data`) |
| `IsolationLevel` | Enum | Describes isolation strategy tier |
| `AuditAction` | String | Action description for audit trail |
| `ProvisioningStatus` | Enum | `PENDING` → `APPLYING` → `DONE` / `FAILED` |
| `AuditScope` | Enum | `platform` or `tenant` |

### 4.5 Domain Events

| Event | Trigger | Consumer SPI | Side Effects |
| --- | --- | --- | --- |
| `TenantGovernanceApplied` | Governance package applied | MultiTenancyPort, AuthPort, ManifestPort | Capsule/Kueue/Keycloak/Manifest materialization |
| `QuotaDeployed` | Quotas delivered to K8s/gateway | — (internal) | Tenant resource limits active |
| `EntitlementDeployed` | Component whitelist set | ManifestPort | Rewrite tenant `PlatformManifest.spec` |
| `UserProvisioned` | User synced to Keycloak | AuthPort | Keycloak user/role synchronization |
| `AuditRecorded` | Any governance change | — (internal) | Immutable `audit_log` entry persisted |
| `ManifestSynced` | Provisioning plan applied | ProvisioningPort | ArgoCD sync initiated; plan status tracking |

### 4.6 Domain Services (Pure Logic)

| Domain Service | Responsibility | Key Rule |
| --- | --- | --- |
| `QuotaDeploymentRule` | Map plan quotas to K8s+Kueue+gateway quotas | GPU quota only for full profile self-hosted inference |
| `EntitlementConsistencyRule` | Validate component whitelist against Manifest dependency graph | `billing` requires `multitenancy`; `multitenancy` requires `auth` |
| `IsolationEnforcementRule` | Build isolation carriers (Namespace, NetworkPolicy, data prefix) | Mandatory "data never leaves tenant" enforcement |
| `ModelWhitelistRule` | Validate model grants against tenant plan | Enterprise-only for restricted models |
| `AdminMinPrivilegeRule` | Enforce platform vs tenant scope separation | Platform admin cannot cross tenant boundaries without explicit scope |
| `OrchestrationPlanRule` | Route component changes through dependency resolver → provisioning engine | All component changes must go through resolver for dependency validation |

---

## 5. SPI Ports & Adapters

### 5.1 Architecture Principle

Domain layer defines Port interfaces only. Infrastructure layer implements Adapters with ACL translation. Nine SPI Ports orchestrate nine downstream managed planes. Multiple implementations coexist; switching requires zero domain changes.

### 5.2 Port Inventory

| Port (domain interface) | SPI Port (bom.yaml) | Default Adapter | ACL Responsibility |
| --- | --- | --- | --- |
| `AuthPort` | `Auth` (§4.7.3) | **Keycloak@25.0.0** ✅ | token/claims → `TenantContext`/`Role`; user lifecycle sync |
| `ControlPlaneClient` | — (§4.7/§8) | REST → `ai-platform-api` | Governance DTO ⇄ platform-api domain objects (anti-corruption) |
| `MultiTenancyPort` | `MultiTenancy` (§8.2) | **Capsule@1.9.0** ✅ (optional, multi-tenant) | Tenant CRD/Quota ⇄ internal `IsolationSpec` |
| `GpuQueuePort` | — (§14.4/§9.3) | Kueue Adapter (full only) | ClusterQueue ⇄ internal `KueueQueue` |
| `ManifestPort` | — (§12) | REST → `ai-dependency-resolver` (Go) | Manifest DTO ⇄ internal `TenantConfig`; dependency graph expansion |
| `ProvisioningPort` | — (§13.3) | REST → `ai-provisioning-engine` (ArgoCD) | Change plan ⇄ deployment instructions |
| `CostPort` | — (§8.3/§14.4) | REST → `ai-billing-service` + OpenCost | Cost/invoice ⇄ internal `Bill` |
| `ModelRegistryPort` | — (§4.4.5) | REST → ModelRegistry | Model supply/auth ⇄ internal `ModelWhitelist` |
| `CachePort` | `Cache` (§4.3.4) | **Redis@7.4.0** ✅ / **Valkey@7.2.0** (optional) | Resource view cache (tenant key prefix) |

### 5.3 Orchestration Flow (Closed-Loop)

```
Component Change Request → Admin Frontend
  → EntitlementConsistencyRule.validate(whitelist, manifest deps)
  → ControlPlaneClient → ai-platform-api.setEntitlements() [authoritative write]
  → MultiTenancyPort → Capsule.deployQuota(NS+Quota+NP) [isolation]
  → ManifestPort → ai-dependency-resolver.expand(deps) [incremental plan]
  → ProvisioningPort → ai-provisioning-engine.apply(ArgoCD sync) [deploy]
  → provisioningPlansRepository.save(status=APPLYING) [track]
  → auditLog.write(AuditEntry) [immutable audit]
```

### 5.4 Resource View Aggregation Flow

```
GET /admin/tenants/{T}/resources
  → ControlPlaneClient → ai-platform-api.getProfile() [allocated quota]
  → MultiTenancyPort → Capsule/K8s.queryUsage() [actual usage]
  → CostPort → ai-billing-service.getTenantCost() [billing data]
  → Aggregate: Allocated + Used + Isolation + Bill → TenantResourceView
```

### 5.5 Multi-Implementation Strategy

| Scenario | Port | Strategy |
| --- | --- | --- |
| Primary + Alternative (P10) | `CachePort` | Redis (default) + Valkey (optional OSI). Both beans; config-selected. |
| Capability Skip (P10) | `MultiTenancyPort` | Capsule only injected for advanced/full; null-object otherwise. |
| Capability Skip (P10) | `GpuQueuePort` | Kueue only injected for full profile; null-object otherwise. |
| Fan-Out Orchestration | Multiple Ports | Single orchestration command fans out to 4-6 managed planes. Each SPI adapter handles its target independently. |

### 5.6 External Dependencies (bom.yaml alignment)

| Integration Point | Type | Version | License | Scope | Port |
| --- | --- | --- | --- | --- | --- |
| Keycloak | External OSS | 25.0.0 | Apache-2.0 | core | AuthPort |
| Capsule | External OSS | 1.9.0 | Apache-2.0 | optional | MultiTenancyPort |
| Redis | External OSS | 7.4.0 | BSD-3 | core | CachePort |
| Valkey | External OSS | 7.2.0 | BSD-3 | optional | CachePort |
| PostgreSQL | External OSS | 16.0 | PostgreSQL | core base | — (direct JPA) |
| Kueue | External OSS | — | Apache-2.0 | full only | GpuQueuePort |
| OpenCost | External OSS | — | Apache-2.0 | advanced+ | CostPort |
| ai-platform-api | Internal (Java) | v1.0.0 | internal | core | ControlPlaneClient |
| ai-dependency-resolver | Internal (Go) | v1.0.0 | internal | core | ManifestPort |
| ai-provisioning-engine | Internal (Go) | v1.0.0 | internal | core | ProvisioningPort |
| ai-billing-service | Internal (Java) | v1.0.0 | internal | advanced+ | CostPort |
| ModelRegistry | Internal/External | — | — | core | ModelRegistryPort |

---

## 6. Key Architectural Decisions

| # | Decision | Rationale | Impact |
| --- | --- | --- | --- |
| ADR-1 | Admin-service as governance orchestrator, NOT data authority | `ai-platform-api` is the single source of truth for tenant/user/quota data. Admin-service dispatches orchestration commands via `ControlPlaneClient` SPI. | Prevents dual-write conflicts; but adds network hop for every governance write. |
| ADR-2 | Governance state persisted locally (tenant_governance, provisioning_plans, audit_log) | Business data lives in platform-api; orchestration state and audit live here for idempotency, tracking, and recovery. | Two datasets to maintain consistency between; governance state mirrors platform-api data. |
| ADR-3 | Resource views are runtime aggregations (not persisted) | Global/Tenant resource views built from multi-source queries (platform-api + Capsule + billing). Cached in Redis; never stored in DB. | Views are always current; but require all sources to be available at query time. |
| ADR-4 | Mandatory dependency graph expansion before provisioning | All component changes go through: `ai-dependency-resolver` → incremental plan → `ai-provisioning-engine` (ArgoCD). | Prevents broken configurations; but adds resolver latency to every component change. |
| ADR-5 | Isolation spec as part of TenantGovernance aggregate | Namespace, NetworkPolicy, KueueQueue, CollectionPrefix, Bucket all materialized atomically when governance is applied. | Ensures consistent isolation; one-step rollback possible. |
| ADR-6 | Immutable audit (audit_log INSERT-ONLY, even when security=off) | All governance changes are audited. Audit is a core capability, not dependent on security profile. | Audit cannot be disabled; storage grows over time — retention policy TBD. |
| ADR-7 | Nine SPI Ports for nine managed planes | Each downstream target has its own Port + Adapter + ACL. No shared or combined ports. | Clean separation; but 9 ACL translations to maintain when external schemas change. |
| ADR-8 | Profile-gated capability injection | Kueue, Capsule, and billing adapters only injected when profile enables them. Null-object adapters for disabled capabilities. | Simplifies single-tenant (starter) deployments; no special-casing in code. |

---

## 7. Service Boundary Diagram

```text
┌──────────────────────────────────────────────────────────────┐
│                    Upstream Consumer                          │
│              ai-admin-frontend (TS + antd)                    │
└────────────────────────────┬─────────────────────────────────┘
                             │ Auth'd, role-based (platform/tenant)
┌────────────────────────────▼─────────────────────────────────┐
│                  ai-admin-service (8088)                       │
│  (core, all profiles; governance orchestrator)                │
│                                                               │
│  ┌────────────────── Application Layer ──────────────────┐   │
│  │ TenantGovAppService    QuotaGovAppService               │   │
│  │ EntitlementGovAppSvc   ModelGovAppService               │   │
│  │ UserGovAppService      GlobalResourceAppService         │   │
│  │ TenantResourceAppSvc   ProvisioningAppService           │   │
│  │ AuditAppService (Read)                                  │   │
│  └────────────────────────────────────────────────────────┘   │
│  ┌────────────────── Domain Layer (3) ────────────────────┐   │
│  │ Aggregates: TenantGovernance (root), AuditEntry (root) │   │
│  │ Views: GlobalResourceView, TenantResourceView           │   │
│  │ Entities: QuotaPolicy, EntitlementSet, ModelWhitelist   │   │
│  │   IsolationSpec, ProvisioningPlan                       │   │
│  │ Value Objects: PackageAssignment, ResourceQuota,        │   │
│  │   NetworkPolicy, KueueQueue, CollectionPrefix, Bucket   │   │
│  │ Domain Services: QuotaDeploymentRule, EntitlementRule,  │   │
│  │   IsolationEnforcementRule, ModelWhitelistRule,          │   │
│  │   AdminMinPrivilegeRule, OrchestrationPlanRule           │   │
│  │ Domain Events: TenantGovernanceApplied, QuotaDeployed,  │   │
│  │   EntitlementDeployed, UserProvisioned, AuditRecorded   │   │
│  │                                                         │   │
│  │ Port Interfaces (pure Java, 9 ports):                   │   │
│  │ AuthPort │ ControlPlaneClient │ MultiTenancyPort        │   │
│  │ GpuQueuePort │ ManifestPort │ ProvisioningPort          │   │
│  │ CostPort │ ModelRegistryPort │ CachePort                │   │
│  └────────────────────────────────────────────────────────┘   │
│  ┌──────────────── Infrastructure Layer (4) ──────────────┐   │
│  │ Adapters:                                               │   │
│  │ KeycloakAdapter │ ControlPlaneClient (ai-platform-api)  │   │
│  │ CapsuleAdapter │ KueueAdapter (full only)               │   │
│  │ ManifestAdapter │ ProvisioningAdapter (ArgoCD)          │   │
│  │ CostAdapter │ ModelRegistryAdapter                      │   │
│  │ RedisAdapter/ValkeyAdapter                              │   │
│  │ JPA: TenantGovernanceEntity, ProvisioningPlanEntity,    │   │
│  │   AuditEntryEntity (INSERT-ONLY)                        │   │
│  │ Flyway: V1__admin_init.sql                              │   │
│  └────────────────────────────────────────────────────────┘   │
└────────────────────────────┬─────────────────────────────────┘
                             │ 9 SPI/ACL call paths
┌────────────────────────────▼─────────────────────────────────┐
│                   Managed Planes (9 Targets)                  │
│  ai-platform-api │ Capsule/K8s │ Kueue │ Keycloak             │
│  ai-dependency-resolver │ ai-provisioning-engine (ArgoCD)    │
│  ai-billing-service │ OpenCost │ ModelRegistry               │
└──────────────────────────────────────────────────────────────┘
```

---

> **References**:
> - Full design: `docs/DESIGN.md` (16 sections)
> - Architecture framework: `../../OpenStrata architecture design document v2.8.md` §14, §8, §4.7, §12, §15.5, §16
> - Governance orchestration: §13.3 (dependency resolver → provisioning engine)
> - Data authority separation: ADR in §16 (open question #1)
