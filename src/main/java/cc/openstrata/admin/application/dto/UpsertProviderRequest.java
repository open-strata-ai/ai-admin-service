package cc.openstrata.admin.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/** Create/update body for an LLM provider (PR-C). */
public record UpsertProviderRequest(
    @NotBlank String name,
    @NotBlank String type,
    String baseUrl,
    String authType,
    String status,
    Map<String, String> labels) {
}
