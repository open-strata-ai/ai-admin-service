package cc.openstrata.admin.application.dto;

import java.util.List;

/** Body to configure the SSO / OIDC IdP (PR-C). */
public record SsoConfigRequest(
    String realm,
    String authServerUrl,
    String clientId,
    String clientSecret,
    List<String> defaultRoles,
    boolean autoSync) {
}
