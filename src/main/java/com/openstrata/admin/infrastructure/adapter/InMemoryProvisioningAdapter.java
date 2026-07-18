package com.openstrata.admin.infrastructure.adapter;

import com.openstrata.admin.domain.port.ProvisioningPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** In-memory ProvisioningPort standing in for ai-provisioning-engine (ArgoCD). */
@Component
public class InMemoryProvisioningAdapter implements ProvisioningPort {

    @Override
    public String apply(String tenantId, String manifest) {
        return "dep-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
