package cc.openstrata.admin.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/** Create/update body for a platform user (PR-C). Roles may include `viewer`. */
public record UpsertUserRequest(
    @NotBlank @Email String email,
    String name,
    String tenantId,
    List<String> roles,
    String status) {
}
