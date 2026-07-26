package cc.openstrata.admin.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Body to set a provider secret (PR-C). The secret is routed to the secret
 *  store and never persisted on the provider row. */
public record SecretRequest(@NotBlank String secret) {
}
