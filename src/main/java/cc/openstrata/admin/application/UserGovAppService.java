package cc.openstrata.admin.application;

import cc.openstrata.admin.application.dto.UserSyncRequest;
import cc.openstrata.admin.config.TenantContext;
import cc.openstrata.admin.domain.model.AuditScope;
import cc.openstrata.admin.domain.port.AuthPort;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Use case: user SSO / lifecycle sync (RULE-07). DESIGN §4. */
@Service
public class UserGovAppService {

    private final AuthPort authPort;
    private final AuditAggregationService audit;

    public UserGovAppService(AuthPort authPort, AuditAggregationService audit) {
        this.authPort = authPort;
        this.audit = audit;
    }

    public void sync(UserSyncRequest req) {
        authPort.syncUser(req.tenantId(), req.userId(), req.roles());
        audit.record(actor(), AuditScope.PLATFORM, req.tenantId(), "USER_SYNCED",
            java.util.Map.of("userId", req.userId(), "roles", String.valueOf(req.roles())));
    }

    public Set<String> list(String tenantId) {
        return authPort.listUsers(tenantId);
    }

    private String actor() {
        String t = TenantContext.tenantId();
        return t == null ? "system" : t;
    }
}
