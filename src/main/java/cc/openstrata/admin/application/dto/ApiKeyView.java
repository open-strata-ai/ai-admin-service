package cc.openstrata.admin.application.dto;

import java.time.Instant;
import java.util.List;

/** Read view of an API key (PR-C). Never exposes the plaintext or hash. */
public record ApiKeyView(
    String id,
    String name,
    String tenantId,
    String ownerEmail,
    String prefix,
    List<String> scopes,
    String status,
    Instant expiresAt,
    Instant lastUsedAt,
    Instant createdAt) {
}
