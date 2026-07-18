package cc.openstrata.admin.application.dto;

import cc.openstrata.admin.domain.model.ResourceQuota;

public record TenantResourceResponse(String tenantId, ResourceQuota allocated,
                                     ResourceQuota used, double cost) {
}
