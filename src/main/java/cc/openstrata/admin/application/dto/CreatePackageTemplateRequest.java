package cc.openstrata.admin.application.dto;

import java.util.List;

/** Request to create a package template (PA-04). */
public record CreatePackageTemplateRequest(String name, String tier, List<String> components, String quotaPolicy) {
}
