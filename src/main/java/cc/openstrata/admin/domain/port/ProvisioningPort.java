package cc.openstrata.admin.domain.port;

/** Provisioning SPI (§13.3). Calls ai-provisioning-engine (ArgoCD). */
public interface ProvisioningPort {
    /** Returns a deployment id used to track the plan status. */
    String apply(String tenantId, String manifest);
}
