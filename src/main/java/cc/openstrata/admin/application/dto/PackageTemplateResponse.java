package cc.openstrata.admin.application.dto;

import cc.openstrata.admin.domain.model.PackageTemplate;
import java.util.List;

/** Response shape for a package template (PA-04). */
public record PackageTemplateResponse(String id, String name, String tier, List<String> components, String quotaPolicy) {
    public static PackageTemplateResponse from(PackageTemplate t) {
        return new PackageTemplateResponse(t.getId(), t.getName(), t.getTier(), t.getComponents(), t.getQuotaPolicy());
    }
}
