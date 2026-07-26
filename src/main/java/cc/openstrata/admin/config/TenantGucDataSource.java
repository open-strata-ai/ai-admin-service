package cc.openstrata.admin.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * Wraps the application {@link DataSource} so that every borrowed connection has
 * the current request's tenant context pushed into PostgreSQL session GUCs. The
 * RLS policies in {@code V4__rls_tenant_isolation.sql} read these GUCs to enforce
 * tenant isolation at the database layer (defense-in-depth).
 *
 * <p>When no {@link TenantContext} is established (startup, Flyway migrations,
 * health checks) the GUCs are left unset and the policies fall back to their
 * bypass branch, so internal maintenance is never blocked. On non-PostgreSQL
 * datasources (e.g. the {@code offline} H2 profile) the GUCs are skipped because
 * H2 has neither RLS nor the {@code set_config} function.
 *
 * <p>The tenant id is bound as a parameter via {@code set_config} rather than
 * string-concatenated SQL, which avoids any SQL-injection surface from the
 * {@code X-Tenant-Id} header / JWT claim.
 */
public class TenantGucDataSource extends DelegatingDataSource {

    public TenantGucDataSource(DataSource delegate) {
        setTargetDataSource(delegate);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection c = super.getConnection();
        applyTenantGucs(c);
        return c;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection c = super.getConnection(username, password);
        applyTenantGucs(c);
        return c;
    }

    private void applyTenantGucs(Connection c) throws SQLException {
        TenantContext.Tenant t = TenantContext.get();
        if (t == null || !isPostgres(c)) {
            return;
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT set_config('app.tenant_id', ?, false),"
              + " set_config('app.platform_admin', ?, false),"
              + " set_config('app.rls_bypass', 'false', false)")) {
            ps.setString(1, t.tenantId() == null ? "" : t.tenantId());
            ps.setString(2, Boolean.toString(t.platformAdmin()));
            ps.execute();
        }
    }

    private boolean isPostgres(Connection c) {
        try {
            return c.getMetaData().getDatabaseProductName()
                    .toLowerCase().contains("postgres");
        } catch (SQLException e) {
            return false;
        }
    }
}
