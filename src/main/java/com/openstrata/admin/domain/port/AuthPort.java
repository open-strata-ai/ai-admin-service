package com.openstrata.admin.domain.port;

import com.openstrata.admin.domain.model.TenantId;
import java.util.Set;

/** Auth SPI (§4.7.3). Keycloak adapter is the default implementation. */
public interface AuthPort {
    void syncUser(String tenantId, String userId, Set<String> roles);
    Set<String> listUsers(String tenantId);
}
