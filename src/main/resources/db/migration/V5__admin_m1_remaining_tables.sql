-- ai-admin-service remaining M1 tables (PR-C).
-- V2__admin_m1.sql was committed empty, so the PR-C tables other than the
-- tenant-scoped directory tables (created in V3) were never materialised. This
-- migration restores the intended DDL. JSON columns are stored as TEXT
-- (portable across Postgres/H2). Columns mirror the JPA entities in
-- cc.openstrata.admin.infrastructure.persistence.
--
-- RLS is intentionally NOT applied here: only the tenant-scoped directory
-- tables (platform_users / api_keys, see V4) are row-isolated by tenant. The
-- tables below are platform-level (providers / models / package_templates /
-- provisioning_plans), governance (tenant_governance) or audit (audit_log) and
-- are read by platform admins without per-row tenant isolation.

CREATE TABLE IF NOT EXISTS tenant_governance (
  tenant_id      VARCHAR(64)  PRIMARY KEY,
  pkg            VARCHAR(64)  NOT NULL,
  quota_policy   TEXT,
  entitlements   TEXT,
  model_whitelist TEXT,
  isolation_spec TEXT,
  updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS providers (
  id          VARCHAR(64)  PRIMARY KEY,
  name        VARCHAR(255) NOT NULL,
  type        VARCHAR(64)  NOT NULL,
  base_url    TEXT,
  auth_type   VARCHAR(32)  NOT NULL DEFAULT 'api_key',
  status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
  secret_ref  VARCHAR(255),
  labels      TEXT,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS models (
  id               VARCHAR(64)  PRIMARY KEY,
  provider_id      VARCHAR(64)  NOT NULL,
  name             VARCHAR(255) NOT NULL,
  family           VARCHAR(64),
  context_window   INTEGER,
  max_output_tokens INTEGER,
  capabilities     TEXT,
  status           VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_models_provider ON models(provider_id);

CREATE TABLE IF NOT EXISTS audit_log (
  id         BIGSERIAL    PRIMARY KEY,
  actor      VARCHAR(255) NOT NULL,
  scope      VARCHAR(64)  NOT NULL,
  tenant_id  VARCHAR(64),
  action     VARCHAR(255) NOT NULL,
  payload    TEXT,
  created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_audit_log_tenant ON audit_log(tenant_id);

CREATE TABLE IF NOT EXISTS package_templates (
  id           VARCHAR(64)  PRIMARY KEY,
  name         VARCHAR(255) NOT NULL,
  tier         VARCHAR(64)  NOT NULL,
  components   TEXT         NOT NULL,
  quota_policy TEXT
);

CREATE TABLE IF NOT EXISTS provisioning_plans (
  plan_id    VARCHAR(64)  PRIMARY KEY,
  tenant_id  VARCHAR(64)  NOT NULL,
  manifest   TEXT         NOT NULL,
  status     VARCHAR(32)  NOT NULL,
  created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_provisioning_plans_tenant ON provisioning_plans(tenant_id);
