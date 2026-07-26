package cc.openstrata.admin.infrastructure.adapter;

import cc.openstrata.admin.domain.port.SecretStorePort;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * In-memory {@link SecretStorePort} for local dev / offline runs that have no
 * Vault reachable (PR-C). Selected when
 * {@code openstrata.spi.secret-provider=inmemory}. Secrets are held in a plain
 * map — never use this outside development.
 */
@Component
@ConditionalOnProperty(name = "openstrata.spi.secret-provider", havingValue = "inmemory")
public class InMemorySecretAdapter implements SecretStorePort {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    @Override
    public String store(String providerId, String secret) {
        String ref = "memory://providers/" + providerId;
        store.put(ref, secret);
        return ref;
    }

    @Override
    public String fetch(String ref) {
        return store.get(ref);
    }

    @Override
    public void delete(String ref) {
        store.remove(ref);
    }
}
