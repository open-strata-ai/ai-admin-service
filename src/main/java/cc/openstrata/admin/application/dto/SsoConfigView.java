package cc.openstrata.admin.application.dto;

import java.util.List;

/** Read view of the SSO / OIDC IdP configuration (PR-C). Never exposes the
 *  client secret — only whether one is configured. */
public record SsoConfigView(
    String realm,
    String authServerUrl,
    String clientId,
    boolean hasClientSecret,
    List<String> defaultRoles,
    boolean autoSync,
    String status) {
}
