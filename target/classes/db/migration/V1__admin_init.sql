-- ai-admin-service governance orchestration schema (schema: admin_gov)
-- NOTE: governance state is serialized as JSON text (portable across Postgres/H2).
-- Production may switch TEXT columns to JSONB without code changes.

CREATE TABLE IF NOT EXISTS tenant_governance (
  tenant_id        VARCHAR(64) PRIMARY KEY,
  package          VARCHAR(32) NOT NULL,
  quota_policy     TEXT        NOT NULL,
  entitlements     TEXT        NOT NULL,
  model_whitelist  TEXT        NOT NULL,
  isolation_spec   TEXT,
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS provisioning_plans (
  plan_id     VARCHAR(64) PRIMARY KEY,
  tenant_id   VARCHAR(64) NOT NULL,
  manifest    TEXT        NOT NULL,
  status      VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Immutable, INSERT-ONLY audit trail (§14.6 / §4.7.4)
CREATE TABLE IF NOT EXISTS audit_log (
  id          BIGSERIAL PRIMARY KEY,
  actor       VARCHAR(64) NOT NULL,
  scope       VARCHAR(8)  NOT NULL,
  tenant_id   VARCHAR(64),
  action      VARCHAR(64) NOT NULL,
  payload     TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_audit_tenant ON audit_log(tenant_id, created_at);
