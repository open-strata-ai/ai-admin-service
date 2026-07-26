package cc.openstrata.admin.application;

import cc.openstrata.admin.application.dto.SsoConfigRequest;
import cc.openstrata.admin.application.dto.SsoConfigView;
import cc.openstrata.admin.application.JsonSupport;
import cc.openstrata.admin.domain.port.AuthPort;
import cc.openstrata.admin.infrastructure.persistence.PlatformUserEntity;
import cc.openstrata.admin.infrastructure.persistence.PlatformUserJpaRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

/**
 * Use case: SSO / OIDC IdP configuration (PR-C). Holds the Keycloak-style config
 * the admin console writes; the actual token exchange happens at the gateway /
 * frontend (PR-A wires real Keycloak OIDC). {@code clientSecret} is delegated to
 * the secret store by the controller and only {@code hasClientSecret} is exposed.
 *
 * <p>Config is kept in-memory for M1 (resets on restart) — persisting it is a
 * follow-up once a config table is introduced.
 */
@Service
public class SsoAppService {

    private final AuthPort authPort;
    private final PlatformUserJpaRepository users;
    private final AtomicReference<SsoConfigRequest> config = new AtomicReference<>();

    public SsoAppService(AuthPort authPort, PlatformUserJpaRepository users) {
        this.authPort = authPort;
        this.users = users;
    }

    public SsoConfigView getConfig() {
        SsoConfigRequest c = config.get();
        if (c == null) {
            return new SsoConfigView(null, null, null, false, List.of(), false, "UNCONFIGURED");
        }
        return new SsoConfigView(c.realm(), c.authServerUrl(), c.clientId(),
            c.clientSecret() != null && !c.clientSecret().isBlank(),
            c.defaultRoles() == null ? List.of() : c.defaultRoles(),
            c.autoSync(), "CONFIGURED");
    }

    public SsoConfigView save(SsoConfigRequest req) {
        // M1: config is in-memory (volatile). The client secret is kept here for
        // the running process; persisting it to the secret store is a follow-up.
        config.set(req);
        return getConfig();
    }

    /** Validate the IdP endpoint is reachable / well-formed (best-effort). */
    public Map<String, Object> testConnection() {
        SsoConfigRequest c = config.get();
        boolean ok = c != null && c.authServerUrl() != null && !c.authServerUrl().isBlank()
            && c.realm() != null && !c.realm().isBlank();
        return Map.of("ok", ok,
            "authServerUrl", c == null ? null : c.authServerUrl(),
            "realm", c == null ? null : c.realm());
    }

    /** Push the local user directory to the IdP (Keycloak) via {@link AuthPort}. */
    public Map<String, Object> sync() {
        List<PlatformUserEntity> all = users.findAll();
        int count = 0;
        for (PlatformUserEntity u : all) {
            Set<String> roles = Set.copyOf(JsonSupport.read(u.getRoles()));
            authPort.syncUser(u.getTenantId() == null ? "platform" : u.getTenantId(),
                u.getEmail(), roles);
            count++;
        }
        return Map.of("synced", count);
    }
}
