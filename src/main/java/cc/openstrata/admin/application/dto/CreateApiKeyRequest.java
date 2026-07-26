package cc.openstrata.admin.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

/** Body to generate a new API key (PR-C). The plaintext is returned once. */
public record CreateApiKeyRequest(
    @NotBlank String name,
    String tenantId,
    String ownerEmail,
    List<String> scopes,
    Instant expiresAt) {
}
