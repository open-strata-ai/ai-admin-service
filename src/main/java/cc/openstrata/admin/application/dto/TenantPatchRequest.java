package cc.openstrata.admin.application.dto;

import jakarta.validation.constraints.NotBlank;

public record TenantPatchRequest(@NotBlank String action) {
    public boolean isSuspend() { return "suspend".equalsIgnoreCase(action); }
    public boolean isResume() { return "resume".equalsIgnoreCase(action); }
}
