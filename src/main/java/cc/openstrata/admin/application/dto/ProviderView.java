package cc.openstrata.admin.application.dto;

import java.time.Instant;
import java.util.Map;

/** Read view of a provider (PR-C). Never exposes the stored secret. */
public record ProviderView(
    String id,
    String name,
    String type,
    String baseUrl,
    String authType,
    String status,
    boolean hasSecret,
    Map<String, String> labels,
    Instant createdAt,
    Instant updatedAt) {
}
