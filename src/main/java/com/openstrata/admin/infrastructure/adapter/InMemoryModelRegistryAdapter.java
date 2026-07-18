package com.openstrata.admin.infrastructure.adapter;

import com.openstrata.admin.domain.port.ModelRegistryPort;
import org.springframework.stereotype.Component;

/** In-memory ModelRegistryPort standing in for ModelRegistry. */
@Component
public class InMemoryModelRegistryAdapter implements ModelRegistryPort {

    @Override
    public void authorize(String tenantId, String provider, String model, boolean restricted) {
        // Model supply / authorization call
    }
}
