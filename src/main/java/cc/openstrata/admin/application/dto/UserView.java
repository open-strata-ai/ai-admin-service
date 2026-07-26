package cc.openstrata.admin.application.dto;

import java.time.Instant;
import java.util.List;

/** Read view of a platform user (PR-C). */
public record UserView(
    String id,
    String email,
    String name,
    String tenantId,
    List<String> roles,
    String status,
    Instant createdAt,
    Instant updatedAt) {
}
