package cc.openstrata.admin.domain.model;

/**
 * Resource quota VO (DESIGN §3 / §8.1 D5). Covers CPU/Mem (always), GPU (full
 * self-hosted only), plus token/QPS/vector for gateway `tenant×model` quotas.
 */
public record ResourceQuota(int cpuCores, int memoryGi, int gpu,
                           long tokenPerMonth, int qps, int vectorCount) {

    public boolean hasGpu() {
        return gpu > 0;
    }
}
