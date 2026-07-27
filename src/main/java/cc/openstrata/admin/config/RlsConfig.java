package cc.openstrata.admin.config;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Registers the {@link TenantGucDataSource} wrapper as the primary
 * {@link DataSource} so that every connection obtained by JPA, Flyway and the
 * transaction manager carries the per-request tenant GUCs that drive the
 * database-level Row-Level Security policies.
 *
 * <p>The delegate is built directly from {@link DataSourceProperties} rather than
 * autowiring the container's {@code dataSource} bean: declaring a {@code @Primary}
 * {@link DataSource} here suppresses Boot's auto-configured {@code dataSource}
 * bean (it is {@code @ConditionalOnMissingBean(DataSource.class)}), so injecting
 * it back would create a chicken-and-egg failure.
 */
@Configuration
public class RlsConfig {

    @Bean
    @Primary
    public DataSource tenantScopedDataSource(DataSourceProperties properties) {
        DataSource delegate = properties.initializeDataSourceBuilder().build();
        return new TenantGucDataSource(delegate);
    }
}
