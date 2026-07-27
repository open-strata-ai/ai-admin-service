-- ai-admin-service governance orchestration schema (schema: admin_gov)
-- NOTE: governance state is serialized as JSON text (portable across Postgres/H2).
-- Production may switch TEXT columns to JSONB without code changes.
-- Column names/types mirror the JPA entities in
-- cc.openstrata.admin.infrastructure.persistence. The governance row uses `pkg`
-- (not `package`) to match TenantGovernanceEntity; this baseline was corrected
-- when Flyway (flyway-core) became the schema source of truth (V5 follow-up).

CREATE TABLE IF NOT EXISTS tenant_governance (
  tenant_id      VARCHAR(64)  PRIMARY KEY,
  pkg            VARCHAR(64)  NOT NULL,
  quota_policy   TEXT,
  entitlements   TEXT,
  model_whitelist TEXT,
  isolation_spec TEXT,
  updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS provisioning_plans (
  plan_id     VARCHAR(64)  PRIMARY KEY,
  tenant_id   VARCHAR(64)  NOT NULL,
  manifest    TEXT         NOT NULL,
  status      VARCHAR(32)  NOT NULL,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Reusable package templates (PA-04). components stored as JSON text (portable
-- across Postgres/H2); production may switch to JSONB without code changes.
CREATE TABLE IF NOT EXISTS package_templates (
  id            VARCHAR(64)  PRIMARY KEY,
  name          VARCHAR(255) NOT NULL,
  tier          VARCHAR(64)  NOT NULL,
  components    TEXT         NOT NULL,
  quota_policy  TEXT
);

-- Immutable, INSERT-ONLY audit trail (§14.6 / §4.7.4)
CREATE TABLE IF NOT EXISTS audit_log (
  id          BIGSERIAL    PRIMARY KEY,
  actor       VARCHAR(255) NOT NULL,
  scope       VARCHAR(64)  NOT NULL,
  tenant_id   VARCHAR(64),
  action      VARCHAR(255) NOT NULL,
  payload     TEXT,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_audit_tenant ON audit_log(tenant_id, created_at);
