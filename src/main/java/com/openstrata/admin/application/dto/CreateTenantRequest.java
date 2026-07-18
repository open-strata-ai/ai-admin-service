package com.openstrata.admin.application.dto;

import com.openstrata.admin.domain.model.PackageTier;
import jakarta.validation.constraints.NotBlank;

public record CreateTenantRequest(
    @NotBlank String tenantId,
    @NotBlank String packageTier) {

    public PackageTier toTier() {
        return PackageTier.valueOf(packageTier.trim().toUpperCase());
    }
}
