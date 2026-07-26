package cc.openstrata.admin.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/** Create/update body for a model (PR-C). */
public record UpsertModelRequest(
    @NotBlank String providerId,
    @NotBlank String name,
    String family,
    Integer contextWindow,
    Integer maxOutputTokens,
    List<String> capabilities,
    String status) {
}
