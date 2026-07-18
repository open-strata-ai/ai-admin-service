package com.openstrata.admin.domain.port;

import com.openstrata.admin.domain.model.TenantId;
import java.util.Set;

/**
 * Manifest SPI (§12). Calls ai-dependency-resolver to expand the dependency
 * graph into an incremental plan (new/reuse/offline) before provisioning.
 */
public interface ManifestPort {
    Set<String> expand(TenantId tenantId, Set<String> components);
}
