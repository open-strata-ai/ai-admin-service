package cc.openstrata.admin.infrastructure.adapter;

import cc.openstrata.admin.domain.model.TenantId;
import cc.openstrata.admin.domain.port.AuthPort;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** In-memory AuthPort standing in for Keycloak (user/role sync). */
@Component
public class InMemoryAuthAdapter implements AuthPort {

    private final Map<String, Set<String>> users = new ConcurrentHashMap<>();

    @Override
    public void syncUser(String tenantId, String userId, Set<String> roles) {
        users.computeIfAbsent(tenantId, k -> new HashSet<>()).addAll(roles);
    }

    @Override
    public Set<String> listUsers(String tenantId) {
        return users.getOrDefault(tenantId, Set.of());
    }
}
