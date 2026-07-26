-- ai-admin-service M1 tenant-scoped directory tables (PR-C follow-up).
-- V2__admin_m1.sql was committed empty, so these tables were never created;
-- this migration restores the intended DDL. JSON columns are stored as TEXT
-- (portable across Postgres/H2); production may switch to JSONB without code
-- changes. Columns mirror the JPA entities in
-- cc.openstrata.admin.infrastructure.persistence.

CREATE TABLE IF NOT EXISTS platform_users (
  id         VARCHAR(64)  PRIMARY KEY,
  email      VARCHAR(255) NOT NULL,
  name       VARCHAR(128),
  tenant_id  VARCHAR(64),
  roles      TEXT,
  status     VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_platform_users_tenant ON platform_users(tenant_id);

CREATE TABLE IF NOT EXISTS api_keys (
  id          VARCHAR(64)  PRIMARY KEY,
  name        VARCHAR(255) NOT NULL,
  tenant_id   VARCHAR(64),
  owner_email VARCHAR(255),
  prefix      VARCHAR(16)  NOT NULL,
  key_hash    TEXT         NOT NULL,
  scopes      TEXT,
  status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
  expires_at  TIMESTAMPTZ,
  last_used_at TIMESTAMPTZ,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_api_keys_tenant ON api_keys(tenant_id);
