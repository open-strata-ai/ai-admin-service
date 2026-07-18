package cc.openstrata.admin.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ModelGrantRequest(
    @NotBlank String provider,
    @NotBlank String model,
    boolean restricted) {
}
