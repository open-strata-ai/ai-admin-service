package cc.openstrata.admin.application.dto;

import cc.openstrata.admin.domain.model.EntitlementSet;
import java.util.Set;

public record EntitlementRequest(Set<String> components) {
    public EntitlementSet toEntitlementSet() {
        return new EntitlementSet(components == null ? Set.of() : components);
    }
}
