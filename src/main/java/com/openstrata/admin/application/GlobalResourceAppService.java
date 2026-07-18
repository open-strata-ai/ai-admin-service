package com.openstrata.admin.application;

import com.openstrata.admin.application.dto.GlobalResourceResponse;
import com.openstrata.admin.config.OpenstrataProperties;
import org.springframework.stereotype.Service;

/** Use case: global resource view (multi-source aggregation). DESIGN §4. */
@Service
public class GlobalResourceAppService {

    private final GpuPoolManagementService gpuPoolManagement;
    private final OpenstrataProperties props;

    public GlobalResourceAppService(GpuPoolManagementService gpuPoolManagement,
                                    OpenstrataProperties props) {
        this.gpuPoolManagement = gpuPoolManagement;
        this.props = props;
    }

    public GlobalResourceResponse view() {
        int availableGpu = props.getFeatures().isGpuPoolEnabled() ? 8 : 0;
        return new GlobalResourceResponse(gpuPoolManagement.view(availableGpu));
    }
}
