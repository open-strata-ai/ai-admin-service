package cc.openstrata.admin.infrastructure.adapter;

import cc.openstrata.admin.config.OpenstrataProperties;
import cc.openstrata.admin.domain.port.SecretStorePort;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Default {@link SecretStorePort} backed by HashiCorp Vault KV v2 (PR-C, M1
 * decision: no shipped secret backend → Vault). Secrets live at
 * {@code <mount>/data/<prefix>/<providerId>}. The returned reference is the full
 * Vault path so {@link #fetch} / {@link #delete} can address it directly.
 *
 * <p>Selected when {@code openstrata.spi.secret-provider} is {@code vault} or
 * unset (the default).
 */
@Component
@ConditionalOnProperty(name = "openstrata.spi.secret-provider", havingValue = "vault", matchIfMissing = true)
public class VaultSecretAdapter implements SecretStorePort {

    private final RestClient client;
    private final String mount;
    private final String prefix;
    private final String token;

    @Autowired
    public VaultSecretAdapter(OpenstrataProperties props) {
        OpenstrataProperties.Vault v = props.getSpi().getVault();
        String baseUrl = v.getUrl() == null ? "http://localhost:8200" : v.getUrl();
        this.client = RestClient.create(baseUrl.replaceAll("/+$", ""));
        this.mount = v.getMount() == null ? "secret" : v.getMount();
        this.prefix = v.getPrefix() == null ? "openstrata/providers" : v.getPrefix();
        this.token = v.getToken() == null ? "" : v.getToken();
    }

    @Override
    public String store(String providerId, String secret) {
        String path = referenceFor(providerId);
        client.post()
            .uri(path)
            .header("X-Vault-Token", token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("data", Map.of("secret", secret)))
            .retrieve()
            .toBodilessEntity();
        return path;
    }

    @Override
    public String fetch(String ref) {
        try {
            Map<String, Object> body = client.get()
                .uri(ref)
                .header("X-Vault-Token", token)
                .retrieve()
                .body(Map.class);
            return extractSecret(body);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    @Override
    public void delete(String ref) {
        try {
            client.delete().uri(ref).header("X-Vault-Token", token).retrieve().toBodilessEntity();
        } catch (RuntimeException ex) {
            // best-effort: absent or already deleted
        }
    }

    private String referenceFor(String providerId) {
        return "/" + mount + "/data/" + prefix + "/" + providerId;
    }

    @SuppressWarnings("unchecked")
    private String extractSecret(Map<String, Object> body) {
        if (body == null) return null;
        Object data = body.get("data");
        if (!(data instanceof Map<?, ?> outer)) return null;
        Object inner = outer.get("data");
        if (!(inner instanceof Map<?, ?> innerMap)) return null;
        Object secret = innerMap.get("secret");
        return secret == null ? null : secret.toString();
    }
}
