package cc.openstrata.admin.application.dto;

import java.time.Instant;
import java.util.List;

/** Read view of a model (PR-C). */
public record ModelView(
    String id,
    String providerId,
    String name,
    String family,
    Integer contextWindow,
    Integer maxOutputTokens,
    List<String> capabilities,
    String status,
    Instant createdAt,
    Instant updatedAt) {
}
