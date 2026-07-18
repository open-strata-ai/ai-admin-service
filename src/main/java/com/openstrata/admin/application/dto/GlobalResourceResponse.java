package com.openstrata.admin.application.dto;

import com.openstrata.admin.application.GpuPoolManagementService.GpuPoolView;

public record GlobalResourceResponse(GpuPoolView gpuPool) {
}
