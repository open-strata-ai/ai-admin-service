package com.openstrata.admin.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record UserSyncRequest(
    @NotBlank String tenantId,
    @NotBlank String userId,
    Set<String> roles) {
}
