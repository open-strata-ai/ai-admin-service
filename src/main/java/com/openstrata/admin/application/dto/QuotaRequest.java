package com.openstrata.admin.application.dto;

import com.openstrata.admin.domain.model.PackageTier;
import com.openstrata.admin.domain.model.QuotaPolicy;
import com.openstrata.admin.domain.model.ResourceQuota;

public record QuotaRequest(int cpuCores, int memoryGi, int gpu,
                           long tokenPerMonth, int qps, int vectorCount,
                           boolean gpuEnabled) {

    public QuotaPolicy toPolicy(PackageTier tier) {
        return new QuotaPolicy(tier,
            new ResourceQuota(cpuCores, memoryGi, gpu, tokenPerMonth, qps, vectorCount),
            gpuEnabled);
    }
}
