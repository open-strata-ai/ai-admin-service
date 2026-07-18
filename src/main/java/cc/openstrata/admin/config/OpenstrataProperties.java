package cc.openstrata.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the `openstrata.*` configuration block (DESIGN §10 / SPECS §3).
 * Profile-gated governance dimensions are expressed as boolean feature flags so
 * the running service can inject capabilities without code changes (ADR-8).
 */
@ConfigurationProperties(prefix = "openstrata")
public class OpenstrataProperties {

    private final Service service = new Service();
    private final Features features = new Features();
    private final Spi spi = new Spi();

    public Service getService() { return service; }
    public Features getFeatures() { return features; }
    public Spi getSpi() { return spi; }

    public static class Service {
        private int port = 8088;
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
    }

    public static class Features {
        private boolean adminEnabled = true;
        private boolean gpuPoolEnabled = false;
        private boolean multiTenantEnabled = false;
        private boolean billingViewEnabled = false;
        private boolean modelRegistryEnabled = true;
        private boolean devMode = false;
        private final WhitelistManifest whitelistManifest = new WhitelistManifest();

        public boolean isAdminEnabled() { return adminEnabled; }
        public void setAdminEnabled(boolean v) { this.adminEnabled = v; }
        public boolean isGpuPoolEnabled() { return gpuPoolEnabled; }
        public void setGpuPoolEnabled(boolean v) { this.gpuPoolEnabled = v; }
        public boolean isMultiTenantEnabled() { return multiTenantEnabled; }
        public void setMultiTenantEnabled(boolean v) { this.multiTenantEnabled = v; }
        public boolean isBillingViewEnabled() { return billingViewEnabled; }
        public void setBillingViewEnabled(boolean v) { this.billingViewEnabled = v; }
        public boolean isModelRegistryEnabled() { return modelRegistryEnabled; }
        public void setModelRegistryEnabled(boolean v) { this.modelRegistryEnabled = v; }
        public boolean isDevMode() { return devMode; }
        public void setDevMode(boolean v) { this.devMode = v; }
        public WhitelistManifest getWhitelistManifest() { return whitelistManifest; }
    }

    public static class WhitelistManifest {
        private String conflictStrategy = "REJECT";
        public String getConflictStrategy() { return conflictStrategy; }
        public void setConflictStrategy(String v) { this.conflictStrategy = v; }
    }

    public static class Spi {
        private String authProvider = "keycloak";
        private String multiTenancyProvider = "capsule";
        private String cacheProvider = "redis";
        public String getAuthProvider() { return authProvider; }
        public void setAuthProvider(String v) { this.authProvider = v; }
        public String getMultiTenancyProvider() { return multiTenancyProvider; }
        public void setMultiTenancyProvider(String v) { this.multiTenancyProvider = v; }
        public String getCacheProvider() { return cacheProvider; }
        public void setCacheProvider(String v) { this.cacheProvider = v; }
    }
}
