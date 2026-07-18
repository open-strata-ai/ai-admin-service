package cc.openstrata.admin.application.dto;

import cc.openstrata.admin.application.GpuPoolManagementService.GpuPoolView;

public record GlobalResourceResponse(GpuPoolView gpuPool) {
}
