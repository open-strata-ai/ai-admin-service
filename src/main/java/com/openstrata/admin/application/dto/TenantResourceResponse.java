package com.openstrata.admin.application.dto;

import com.openstrata.admin.domain.model.ResourceQuota;

public record TenantResourceResponse(String tenantId, ResourceQuota allocated,
                                     ResourceQuota used, double cost) {
}
