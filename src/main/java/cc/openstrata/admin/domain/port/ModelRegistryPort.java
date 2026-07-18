package cc.openstrata.admin.domain.port;

/** Model registry SPI (§4.4.5). Model supply / authorization. */
public interface ModelRegistryPort {
    void authorize(String tenantId, String provider, String model, boolean restricted);
}
