-- True PostgreSQL Row-Level Security for tenant-scoped directory tables
-- (PR-D follow-up). Defense-in-depth: even if application code forgets to
-- scope a query, the database enforces tenant isolation.
--
-- The application sets three session GUCs per request (see TenantGucDataSource):
--   app.tenant_id     -> the caller's tenant (from X-Tenant-Id / JWT claim)
--   app.platform_admin-> 'true' when the caller is a platform-admin
--   app.rls_bypass    -> 'false' for normal requests; UNSET for internal
--                        maintenance (Flyway, schema validation, health) which
--                        is therefore exempt.
--
-- FORCE ROW LEVEL SECURITY makes the policy apply even to the table-owning
-- role, so the application connection itself is constrained.

ALTER TABLE platform_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE platform_users FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS platform_users_isolation ON platform_users;
CREATE POLICY platform_users_isolation ON platform_users
  FOR ALL
  USING (
    current_setting('app.rls_bypass', true) IS NULL
    OR current_setting('app.rls_bypass', true) = 'true'
    OR current_setting('app.platform_admin', true) = 'true'
    OR tenant_id = current_setting('app.tenant_id', true)
  )
  WITH CHECK (
    current_setting('app.rls_bypass', true) IS NULL
    OR current_setting('app.rls_bypass', true) = 'true'
    OR current_setting('app.platform_admin', true) = 'true'
    OR tenant_id = current_setting('app.tenant_id', true)
  );

ALTER TABLE api_keys ENABLE ROW LEVEL SECURITY;
ALTER TABLE api_keys FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS api_keys_isolation ON api_keys;
CREATE POLICY api_keys_isolation ON api_keys
  FOR ALL
  USING (
    current_setting('app.rls_bypass', true) IS NULL
    OR current_setting('app.rls_bypass', true) = 'true'
    OR current_setting('app.platform_admin', true) = 'true'
    OR tenant_id = current_setting('app.tenant_id', true)
  )
  WITH CHECK (
    current_setting('app.rls_bypass', true) IS NULL
    OR current_setting('app.rls_bypass', true) = 'true'
    OR current_setting('app.platform_admin', true) = 'true'
    OR tenant_id = current_setting('app.tenant_id', true)
  );
