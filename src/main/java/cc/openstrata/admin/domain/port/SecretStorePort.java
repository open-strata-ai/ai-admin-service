package cc.openstrata.admin.domain.port;

/**
 * Secret storage SPI (PR-C). Provider credentials are never stored inline in the
 * {@code providers} table — only an opaque reference returned by {@link #store}
 * is persisted. The default implementation is HashiCorp Vault (M1 decision: no
 * shipped default → Vault); an in-memory adapter is available for local dev.
 */
public interface SecretStorePort {

    /** Persist a secret and return a stable reference used to retrieve it later. */
    String store(String providerId, String secret);

    /** Retrieve a secret by reference, or {@code null} if absent/forbidden. */
    String fetch(String ref);

    /** Delete a secret by reference (no-op if absent). */
    void delete(String ref);
}
