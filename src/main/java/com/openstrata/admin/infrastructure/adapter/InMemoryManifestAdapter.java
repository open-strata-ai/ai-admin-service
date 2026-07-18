package com.openstrata.admin.infrastructure.adapter;

import com.openstrata.admin.domain.model.TenantId;
import com.openstrata.admin.domain.port.ManifestPort;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * In-memory ManifestPort standing in for ai-dependency-resolver. Expands the
 * requested components into a resolved plan (here: the input set plus transitive
 * `auth` when `multitenancy` is requested, mirroring the §12.4 chain).
 */
@Component
public class InMemoryManifestAdapter implements ManifestPort {

    @Override
    public Set<String> expand(TenantId tenantId, Set<String> components) {
        Set<String> plan = new LinkedHashSet<>(components);
        if (plan.contains("multitenancy")) {
            plan.add("auth");
        }
        if (plan.contains("billing")) {
            plan.add("multitenancy");
            plan.add("auth");
        }
        return plan;
    }
}
