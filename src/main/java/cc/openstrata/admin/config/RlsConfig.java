package cc.openstrata.admin.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Registers the {@link TenantGucDataSource} wrapper as the primary
 * {@link DataSource} so that every connection obtained by JPA, Flyway and the
 * transaction manager carries the per-request tenant GUCs that drive the
 * database-level Row-Level Security policies.
 */
@Configuration
public class RlsConfig {

    @Bean
    @Primary
    public DataSource tenantScopedDataSource(@Qualifier("dataSource") DataSource delegate) {
        return new TenantGucDataSource(delegate);
    }
}
